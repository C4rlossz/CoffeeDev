package dev.coffeedev.data

import android.content.Context
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class LocalDatabaseTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    @Before fun clean() {
        context.deleteDatabase(LocalDatabase.NAME)
        context.getSharedPreferences("coffeedev_demo", Context.MODE_PRIVATE).edit().clear().commit()
    }
    @After fun remove() { context.deleteDatabase(LocalDatabase.NAME) }
    @Test fun valuesSurviveClosingAndReopening() {
        LocalDatabase(context).use { it.putStrings(mapOf("orders" to "[{\"totalCents\":1490}]", "session" to "owner")) }
        LocalDatabase(context).use {
            assertEquals("[{\"totalCents\":1490}]", it.getString("orders", null))
            assertEquals("owner", it.getString("session", null))
        }
    }
    @Test fun oldLocalDataIsMigratedOnlyOnce() {
        val previous = context.getSharedPreferences("coffeedev_demo", Context.MODE_PRIVATE)
        previous.edit().putString("users", "existing-accounts").putString("orders", "existing-orders").commit()
        LocalDatabase(context).use {
            assertEquals("existing-accounts", it.getString("users", null))
            assertEquals("existing-orders", it.getString("orders", null))
            it.putStrings(mapOf("orders" to "new-orders"))
        }
        LocalDatabase(context).use { assertEquals("new-orders", it.getString("orders", null)) }
    }
    @Test fun signOutRemovesOnlySessionAndKeepsOrders() {
        LocalDatabase(context).use {
            it.putStrings(mapOf("orders" to "order-history", "session" to "owner"))
            it.putStrings(mapOf("session" to null))
            assertNull(it.getString("session", null))
            assertEquals("order-history", it.getString("orders", null))
        }
    }
}
