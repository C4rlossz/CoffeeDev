package dev.coffeedev.ui

import android.app.Application
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.coffeedev.data.Catalog
import dev.coffeedev.data.Preferences
import dev.coffeedev.data.RepositoryFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class CoffeeViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = Preferences(application)
    private val repository = RepositoryFactory.create(application)
    val member = repository.member
    val products = repository.products
    val orders = repository.orders
    val members = repository.members
    val failure = repository.failure
    var language by mutableStateOf(preferences.language); private set
    var theme by mutableStateOf(preferences.theme); private set
    var cart by mutableStateOf<Map<String, Int>>(emptyMap()); private set
    var favorites by mutableStateOf<Set<String>>(emptySet()); private set
    var privacyExport by mutableStateOf<String?>(null); private set
    var tab by mutableStateOf("home")
    var busy by mutableStateOf(false); private set
    var loading by mutableStateOf(true); private set
    var message by mutableStateOf<String?>(null); private set
    private var cartOwner: String? = null
    private var orderRequestId: String? = null
    init {
        viewModelScope.launch {
            member.collectLatest { account ->
                if (cartOwner != account?.uid) {
                    cartOwner = account?.uid; tab = "home"; orderRequestId = account?.let { preferences.requestId(it.uid) }
                    cart = account?.let { preferences.cart(it.uid) } ?: emptyMap()
                    favorites = account?.let { preferences.favorites(it.uid) } ?: emptySet()
                }
                if (account?.status != "active") tab = "home"
            }
        }
        viewModelScope.launch {
            try { repository.refresh() } catch (_: Exception) { message = "syncError" } finally { loading = false }
        }
    }
    fun updateLanguage(value: String) { if (value in listOf("pt", "en", "es")) { language = value; preferences.language = value } }
    fun updateTheme(value: String) { theme = value; preferences.theme = value }
    fun clearMessage() { message = null }
    private fun perform(error: String = "error", success: String? = null, action: suspend () -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try { action(); if (success != null) message = success }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { message = error }
            finally { busy = false }
        }
    }
    fun authenticate(name: String, email: String, password: String, registration: Boolean) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() || password.length < 8 || (registration && name.trim().length !in 2..80)) { message = "invalidForm"; return }
        perform(if (registration) "registerError" else "authError") {
            if (registration) repository.register(name, email, password) else repository.signIn(email, password)
        }
    }
    fun exportPersonalData() = perform {
        privacyExport = repository.exportPersonalData()
    }
    fun clearExport() { privacyExport = null }
    fun deleteLocalAccount() = perform {
        repository.deleteLocalAccount(); privacyExport = null; tab = "home"
    }
    fun refresh() = perform { repository.refresh() }
    fun signOut() = perform { repository.signOut(); privacyExport = null }
    fun toggleFavorite(id: String) {
        if (id !in Catalog.ids) return
        favorites = if (id in favorites) favorites - id else favorites + id
        member.value?.let { preferences.saveFavorites(it.uid, favorites) }
    }
    fun changeQuantity(id: String, amount: Int) {
        if (busy || member.value?.status != "active" || id !in Catalog.ids) return
        if (amount > 0 && products.value.none { it.id == id && it.available }) return
        val next = (cart[id] ?: 0) + amount
        if (next > 10) { message = "limit"; return }
        cart = if (next <= 0) cart - id else cart + (id to next)
        orderRequestId = null
        member.value?.let { preferences.saveCart(it.uid, cart); preferences.saveRequestId(it.uid, null) }
    }
    fun add(id: String, quantity: Int) { changeQuantity(id, quantity); if (cart.containsKey(id)) message = "added" }
    fun checkout(note: String) {
        if (busy || cart.isEmpty()) return
        val content = cart.toMap()
        if (runCatching { Catalog.total(content, products.value) }.isFailure) { message = "cartChanged"; return }
        // Retain the id across retries, preventing duplicate orders on network failures.
        val requestId = orderRequestId ?: UUID.randomUUID().toString().also { id -> orderRequestId = id; member.value?.let { preferences.saveRequestId(it.uid, id) } }
        perform("cartChanged", "placed") {
            repository.placeOrder(content, note.take(240), requestId)
            cart = emptyMap(); member.value?.let { preferences.saveCart(it.uid, cart); preferences.saveRequestId(it.uid, null) }; orderRequestId = null; tab = "orders"
        }
    }
    fun access(uid: String, active: Boolean) = perform(success = "saved") { repository.setAccess(uid, active) }
    fun advance(id: String, current: String) {
        val next = when(current) { "received" -> "preparing"; "preparing" -> "ready"; "ready" -> "completed"; else -> return }
        perform(success = "saved") { repository.setOrderStatus(id, next) }
    }
    fun saveProduct(id: String, cents: Int, available: Boolean) {
        if (cents !in 100..100000) { message = "error"; return }
        perform(success = "saved") { repository.setProduct(id, cents, available) }
    }
    override fun onCleared() { repository.close(); super.onCleared() }
}
