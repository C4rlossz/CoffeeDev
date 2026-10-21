package dev.coffeedev.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object RepositoryFactory {
    fun create(context: Context): CoffeeRepository {
        checkNotNull(FirebaseApp.initializeApp(context)) { "Firebase configuration missing" }
        return FirebaseRepository()
    }
}

private class FirebaseRepository : CoffeeRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance().also {
        // Access revocation must not expose cached business data after sign-out.
        it.firestoreSettings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
    }
    private val accountState = MutableStateFlow<Member?>(null)
    private val menuState = MutableStateFlow<List<Product>>(emptyList())
    private val orderState = MutableStateFlow<List<CoffeeOrder>>(emptyList())
    private val memberState = MutableStateFlow<List<Member>>(emptyList())
    private val errorState = MutableStateFlow(false)
    override val member = accountState.asStateFlow()
    override val products = menuState.asStateFlow()
    override val orders = orderState.asStateFlow()
    override val members = memberState.asStateFlow()
    override val failure = errorState.asStateFlow()
    private var accessListener: ListenerRegistration? = null
    private val contentListeners = mutableListOf<ListenerRegistration>()
    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await(); refresh()
    }
    override suspend fun register(name: String, email: String, password: String) {
        val user = requireNotNull(auth.createUserWithEmailAndPassword(email.trim(), password).await().user)
        db.collection("access").document(user.uid).set(mapOf("uid" to user.uid, "name" to name.trim(), "email" to user.email, "status" to "pending")).await()
        refresh()
    }
    override suspend fun refresh() {
        val user = auth.currentUser ?: return
        val ownerId = db.document("system/owner").get(Source.SERVER).await().getString("uid")
        val ref = db.collection("access").document(user.uid)
        if (!ref.get(Source.SERVER).await().exists()) {
            // Repairs a registration interrupted between Auth and Firestore writes.
            ref.set(mapOf("uid" to user.uid, "name" to (user.email?.substringBefore('@')?.takeIf { it.length >= 2 }?.take(80) ?: "CoffeeDev"), "email" to user.email, "status" to "pending")).await()
        }
        accessListener?.remove()
        accessListener = ref.addSnapshotListener { snap, error ->
            if (error != null) {
                errorState.value = true; clearContent()
                accountState.value = Member(user.uid, "", user.email.orEmpty(), "pending"); return@addSnapshotListener
            }
            errorState.value = false
            val before = accountState.value
            val account = Member(user.uid, snap?.getString("name").orEmpty(), user.email.orEmpty(), snap?.getString("status") ?: "pending", user.uid == ownerId)
            accountState.value = account
            if (account.status == "active" && (before?.uid != account.uid || before?.status != "active" || contentListeners.isEmpty())) watchContent(account)
            else if (account.status != "active") clearContent()
        }
    }
    private fun watchContent(account: Member) {
        clearContent()
        contentListeners += db.document("catalog/menu").addSnapshotListener { result, error ->
            if (error != null) { errorState.value = true; menuState.value = emptyList() }
            else menuState.value = Catalog.products.mapNotNull { product ->
                val raw = result?.get("products.${product.id}") as? Map<*, *> ?: return@mapNotNull null
                val price = (raw["priceCents"] as? Number)?.toInt() ?: return@mapNotNull null
                product.copy(priceCents = price, available = raw["available"] as? Boolean ?: false)
            }
        }
        val query = if (account.owner) db.collection("orders").orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(100) else db.collection("orders").whereEqualTo("uid", account.uid).orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(100)
        contentListeners += query.addSnapshotListener { result, error ->
            if (error != null) { errorState.value = true; orderState.value = emptyList() }
            else orderState.value = result?.documents.orEmpty().map { d ->
                @Suppress("UNCHECKED_CAST")
                val quantities = (d.get("quantities") as? Map<String, Number>).orEmpty().mapValues { it.value.toInt() }
                CoffeeOrder(d.id, d.getString("uid").orEmpty(), quantities, (d.getLong("totalCents") ?: 0).toInt(), d.getString("status") ?: "received", d.getTimestamp("createdAt")?.toDate()?.time ?: 0, d.getString("note").orEmpty())
            }.sortedByDescending { it.createdAt }
        }
        if (account.owner) contentListeners += db.collection("access").addSnapshotListener { result, error ->
            if (error != null) { errorState.value = true; memberState.value = emptyList() }
            else memberState.value = result?.documents.orEmpty().map { d -> Member(d.id, d.getString("name").orEmpty(), d.getString("email").orEmpty(), d.getString("status") ?: "pending", d.id == account.uid) }
        }
    }
    private fun clearContent() {
        contentListeners.forEach { it.remove() }; contentListeners.clear()
        menuState.value = emptyList(); orderState.value = emptyList(); memberState.value = emptyList()
    }
    override suspend fun signOut() { close(); auth.signOut(); accountState.value = null; errorState.value = false }
    override suspend fun placeOrder(cart: Map<String, Int>, note: String, requestId: String) {
        val account = requireNotNull(accountState.value); check(account.status == "active")
        // Rules recalculate the total against the authoritative Firestore catalog.
        val total = Catalog.total(cart, products.value)
        val ref = db.collection("orders").document(requestId)
        db.runTransaction { transaction ->
            if (!transaction.get(ref).exists()) transaction.set(ref, mapOf("uid" to account.uid, "quantities" to cart, "totalCents" to total, "status" to "received", "createdAt" to FieldValue.serverTimestamp(), "note" to note.take(240)))
            Unit
        }.await()
    }
    override suspend fun setAccess(uid: String, approved: Boolean) {
        check(member.value?.owner == true && uid != member.value?.uid)
        db.collection("access").document(uid).update("status", if (approved) "active" else "blocked").await()
    }
    override suspend fun setOrderStatus(id: String, status: String) { db.collection("orders").document(id).update("status", status).await() }
    override suspend fun setProduct(id: String, priceCents: Int, available: Boolean) {
        require(id in Catalog.ids && priceCents in 100..100000)
        db.document("catalog/menu").update(mapOf("products.$id.priceCents" to priceCents, "products.$id.available" to available)).await()
    }
    override fun close() { accessListener?.remove(); accessListener = null; clearContent() }
}
