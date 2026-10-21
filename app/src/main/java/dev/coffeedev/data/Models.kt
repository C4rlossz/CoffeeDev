package dev.coffeedev.data

data class Product(val id: String, val category: String, val priceCents: Int, val available: Boolean = true)
data class Member(val uid: String, val name: String, val email: String, val status: String, val owner: Boolean = false)
data class CoffeeOrder(val id: String, val uid: String, val quantities: Map<String, Int>, val totalCents: Int, val status: String, val createdAt: Long, val note: String = "")
object Catalog {
    val products = listOf(
        Product("cappuccino", "hot", 1490), Product("espresso", "hot", 890),
        Product("latte", "hot", 1390), Product("coldbrew", "cold", 1690),
        Product("mocha", "hot", 1590), Product("cookie", "food", 990),
        Product("croissant", "food", 1290), Product("paochapa", "food", 990),
        Product("paoqueijo", "food", 790), Product("brownie", "food", 1190)
    )
    val ids = products.map { it.id }.toSet()
    fun total(cart: Map<String, Int>, menu: List<Product>): Int {
        require(cart.isNotEmpty() && cart.keys.all { it in ids })
        require(cart.values.all { it in 1..10 })
        return cart.entries.sumOf { (id, quantity) ->
            val item = menu.first { it.id == id }; require(item.available)
            Math.multiplyExact(item.priceCents, quantity)
        }
    }
}
interface CoffeeRepository {
    val member: kotlinx.coroutines.flow.StateFlow<Member?>
    val products: kotlinx.coroutines.flow.StateFlow<List<Product>>
    val orders: kotlinx.coroutines.flow.StateFlow<List<CoffeeOrder>>
    val members: kotlinx.coroutines.flow.StateFlow<List<Member>>
    val failure: kotlinx.coroutines.flow.StateFlow<Boolean>
    suspend fun signIn(email: String, password: String)
    suspend fun register(name: String, email: String, password: String)
    suspend fun refresh()
    suspend fun signOut()
    suspend fun placeOrder(cart: Map<String, Int>, note: String, requestId: String)
    suspend fun setAccess(uid: String, approved: Boolean)
    suspend fun setOrderStatus(id: String, status: String)
    suspend fun setProduct(id: String, priceCents: Int, available: Boolean)
    suspend fun exportPersonalData(): String = error("Manual privacy request required")
    suspend fun deleteLocalAccount() { error("Manual privacy request required") }
    fun close()
}
