package dev.coffeedev.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object RepositoryFactory { fun create(context: Context): CoffeeRepository = DemoRepository(context) }

/** Local sandbox only. This source set is excluded from cloud builds. */
private class DemoRepository(context: Context) : CoffeeRepository {
    private val store = LocalDatabase(context)
    private val preferences = Preferences(context)
    private val accountState = MutableStateFlow<Member?>(null)
    private val menuState = MutableStateFlow(Catalog.products)
    private val orderState = MutableStateFlow<List<CoffeeOrder>>(emptyList())
    private val memberState = MutableStateFlow<List<Member>>(emptyList())
    override val member = accountState.asStateFlow()
    override val products = menuState.asStateFlow()
    override val orders = orderState.asStateFlow()
    override val members = memberState.asStateFlow()
    override val failure = MutableStateFlow(false).asStateFlow()
    private var users = JSONArray(store.getString("users", "[]"))
    private var history = JSONArray(store.getString("orders", "[]"))
    init {
        if (users.length() == 0) {
            users.put(user("owner", "Carlos Eduardo", "carlos@coffeedev.demo", "CoffeeDev2026!", "active", true)); persist()
        }
        val prices = JSONObject(store.getString("menu", "{}")!!)
        menuState.value = Catalog.products.map { p -> prices.optJSONObject(p.id)?.let { p.copy(priceCents = it.getInt("price"), available = it.getBoolean("available")) } ?: p }
        update()
    }
    private fun user(uid: String, name: String, email: String, password: String, status: String, owner: Boolean): JSONObject {
        val salt = PasswordHasher.newSalt()
        return JSONObject().put("uid", uid).put("name", name).put("email", email).put("status", status).put("owner", owner).put("salt", salt).put("hash", PasswordHasher.hash(password, salt)).put("algorithm", "pbkdf2-sha256").put("iterations", PasswordHasher.ITERATIONS)
    }
    private fun account(j: JSONObject) = Member(j.getString("uid"), j.getString("name"), j.getString("email"), j.getString("status"), j.getBoolean("owner"))
    private fun allUsers() = (0 until users.length()).map { users.getJSONObject(it) }
    private fun persist() { store.putStrings(mapOf("users" to users.toString(), "orders" to history.toString())) }
    private fun update() {
        val id = store.getString("session", null)
        val current = allUsers().find { it.getString("uid") == id }?.let(::account)
        accountState.value = current
        memberState.value = if (current?.owner == true) allUsers().map(::account) else emptyList()
        orderState.value = if (current?.status != "active") emptyList() else (0 until history.length()).map { index ->
            val j = history.getJSONObject(index); val quantities = j.getJSONObject("quantities")
            CoffeeOrder(j.getString("id"), j.getString("uid"), quantities.keys().asSequence().associateWith { quantities.getInt(it) }, j.getInt("totalCents"), j.getString("status"), j.getLong("createdAt"), j.getString("note"))
        }.filter { current.owner || it.uid == current.uid }.sortedByDescending { it.createdAt }
    }
    override suspend fun signIn(email: String, password: String) {
        val found = allUsers().find { it.getString("email") == email.trim().lowercase() } ?: error("auth")
        check(withContext(Dispatchers.Default) { PasswordHasher.verify(password, found) }) { "auth" }
        if (found.optString("algorithm") != "pbkdf2-sha256") {
            val salt = PasswordHasher.newSalt()
            found.put("salt", salt).put("hash", withContext(Dispatchers.Default) { PasswordHasher.hash(password, salt) }).put("algorithm", "pbkdf2-sha256").put("iterations", PasswordHasher.ITERATIONS)
            persist()
        }
        store.putStrings(mapOf("session" to found.getString("uid"))); update()
    }
    override suspend fun register(name: String, email: String, password: String) {
        require(allUsers().none { it.getString("email") == email.trim().lowercase() })
        val id = UUID.randomUUID().toString()
        users.put(withContext(Dispatchers.Default) { user(id, name.trim(), email.trim().lowercase(), password, "pending", false) }); persist()
        store.putStrings(mapOf("session" to id)); update()
    }
    override suspend fun refresh() { update() }
    override suspend fun signOut() { store.putStrings(mapOf("session" to null)); update() }
    override suspend fun placeOrder(cart: Map<String, Int>, note: String, requestId: String) {
        val current = requireNotNull(member.value); check(current.status == "active")
        if ((0 until history.length()).any { history.getJSONObject(it).getString("id") == requestId }) return
        val total = Catalog.total(cart, products.value)
        history.put(JSONObject().put("id", requestId).put("uid", current.uid).put("quantities", JSONObject(cart)).put("totalCents", total).put("status", "received").put("createdAt", System.currentTimeMillis()).put("note", note.take(240)))
        persist(); update()
    }
    override suspend fun setAccess(uid: String, approved: Boolean) {
        check(member.value?.owner == true && uid != "owner")
        allUsers().first { it.getString("uid") == uid }.put("status", if (approved) "active" else "blocked"); persist(); update()
    }
    override suspend fun setOrderStatus(id: String, status: String) {
        check(member.value?.owner == true)
        val j = (0 until history.length()).map { history.getJSONObject(it) }.first { it.getString("id") == id }
        check(nextStatus(j.getString("status")) == status); j.put("status", status); persist(); update()
    }
    override suspend fun setProduct(id: String, priceCents: Int, available: Boolean) {
        check(member.value?.owner == true && priceCents in 100..100000)
        menuState.value = menuState.value.map { if (it.id == id) it.copy(priceCents = priceCents, available = available) else it }
        val j = JSONObject(); products.value.forEach { j.put(it.id, JSONObject().put("price", it.priceCents).put("available", it.available)) }
        store.putStrings(mapOf("menu" to j.toString()))
    }
    override suspend fun exportPersonalData(): String {
        val current = requireNotNull(member.value)
        val personalOrders = JSONArray()
        (0 until history.length()).map { history.getJSONObject(it) }.filter { it.getString("uid") == current.uid }.forEach { personalOrders.put(it) }
        return JSONObject().put("schema", "coffeedev-personal-data-v1").put("version", "1.2.0")
            .put("account", JSONObject().put("uid", current.uid).put("name", current.name).put("email", current.email).put("status", current.status).put("owner", current.owner))
            .put("orders", personalOrders).put("preferences", preferences.exportedSettings(current.uid)).toString(2)
    }
    override suspend fun deleteLocalAccount() {
        val current = requireNotNull(member.value)
        check(!current.owner) { "Reset application data to remove the fixed demo owner" }
        users = JSONArray(allUsers().filter { it.getString("uid") != current.uid })
        history = JSONArray((0 until history.length()).map { history.getJSONObject(it) }.filter { it.getString("uid") != current.uid })
        store.putStrings(mapOf("users" to users.toString(), "orders" to history.toString(), "session" to null))
        preferences.removeAccount(current.uid)
        update()
    }
    override fun close() { store.close() }
}

private fun nextStatus(status: String) = when(status) { "received" -> "preparing"; "preparing" -> "ready"; "ready" -> "completed"; else -> "" }
