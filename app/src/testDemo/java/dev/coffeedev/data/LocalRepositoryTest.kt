package dev.coffeedev.data

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class LocalRepositoryTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    @Before fun clean() {
        context.deleteDatabase(LocalDatabase.NAME)
        context.getSharedPreferences("coffeedev_demo", Context.MODE_PRIVATE).edit().clear().commit()
    }
    @Test fun approvedOwnerCanOrderAllTenProductsWithoutDuplicateRetry() = runBlocking {
        val repo = RepositoryFactory.create(context)
        try {
            repo.signIn("carlos@coffeedev.demo", "CoffeeDev2026!")
            assertTrue(repo.member.value!!.owner)
            assertEquals(10, repo.products.value.size)
            val cart = Catalog.products.associate { it.id to 1 }
            repo.placeOrder(cart, "No sugar", "test-request")
            repo.placeOrder(cart, "No sugar", "test-request")
            assertEquals(1, repo.orders.value.size)
            assertEquals(Catalog.products.sumOf { it.priceCents }, repo.orders.value.single().totalCents)
        } finally { repo.close() }
        val reopened = RepositoryFactory.create(context)
        try { assertEquals("test-request", reopened.orders.value.single().id) } finally { reopened.close() }
    }
    @Test fun registrationWaitsForOwnerApprovalAndSuspensionBlocksOrders() = runBlocking {
        val repo = RepositoryFactory.create(context)
        try {
            repo.register("Visitor", "visitor@example.com", "Password123!")
            val uid = repo.member.value!!.uid
            assertEquals("pending", repo.member.value!!.status)
            var rejected = false
            try { repo.placeOrder(mapOf("croissant" to 1), "", "denied") } catch (_: IllegalStateException) { rejected = true }
            assertTrue(rejected)
            repo.signOut(); repo.signIn("carlos@coffeedev.demo", "CoffeeDev2026!")
            repo.setAccess(uid, true)
            repo.signOut(); repo.signIn("visitor@example.com", "Password123!")
            assertEquals("active", repo.member.value!!.status)
            repo.placeOrder(mapOf("croissant" to 1), "", "approved")
            assertEquals(1, repo.orders.value.size)
            repo.signOut(); repo.signIn("carlos@coffeedev.demo", "CoffeeDev2026!")
            repo.setAccess(uid, false)
            repo.signOut(); repo.signIn("visitor@example.com", "Password123!")
            assertEquals("blocked", repo.member.value!!.status)
            assertTrue(repo.orders.value.isEmpty())
        } finally { repo.close() }
    }
    @Test fun newFoodPricesAndAvailabilityPersist() = runBlocking {
        val repo = RepositoryFactory.create(context)
        try {
            repo.signIn("carlos@coffeedev.demo", "CoffeeDev2026!")
            repo.setProduct("paoqueijo", 890, false)
        } finally { repo.close() }
        val reopened = RepositoryFactory.create(context)
        try {
            val product = reopened.products.value.first { it.id == "paoqueijo" }
            assertEquals(890, product.priceCents)
            assertFalse(product.available)
            assertTrue(runCatching { Catalog.total(mapOf("paoqueijo" to 1), reopened.products.value) }.isFailure)
        } finally { reopened.close() }
    }
}
