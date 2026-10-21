package dev.coffeedev.data

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class PrivacyTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    @Before fun clean() {
        context.deleteDatabase(LocalDatabase.NAME)
        context.getSharedPreferences("coffeedev_demo", 0).edit().clear().commit()
        context.getSharedPreferences("coffeedev_settings", 0).edit().clear().commit()
    }
    @Test fun exportIsScopedAndNeverContainsCredentials() = runBlocking {
        val repo = RepositoryFactory.create(context)
        try {
            repo.register("Visitor", "visitor@example.com", "TestOnly123!")
            val uid = repo.member.value!!.uid
            repo.signIn("carlos@coffeedev.demo", "CoffeeDev2026!"); repo.setAccess(uid, true)
            repo.placeOrder(mapOf("espresso" to 1), "owner private", "owner-order")
            repo.signIn("visitor@example.com", "TestOnly123!")
            repo.placeOrder(mapOf("croissant" to 1), "visitor note", "visitor-order")
            val text = repo.exportPersonalData(); val data = JSONObject(text)
            assertEquals(uid, data.getJSONObject("account").getString("uid"))
            assertEquals(1, data.getJSONArray("orders").length())
            assertFalse(text.contains("owner-order")); assertFalse(text.contains("owner private"))
            assertFalse(text.contains("hash")); assertFalse(text.contains("salt")); assertFalse(text.contains("TestOnly123!"))
        } finally { repo.close() }
    }
    @Test fun deletionRemovesPendingAccountAndPreferencesWithoutTouchingOthers() = runBlocking {
        val repo = RepositoryFactory.create(context)
        try {
            repo.register("Visitor", "visitor@example.com", "TestOnly123!")
            val uid = repo.member.value!!.uid
            Preferences(context).saveFavorites(uid, setOf("espresso"))
            Preferences(context).saveCart(uid, mapOf("espresso" to 1))
            Preferences(context).saveRequestId(uid, "request")
            repo.deleteLocalAccount(); assertNull(repo.member.value)
            assertTrue(Preferences(context).favorites(uid).isEmpty()); assertTrue(Preferences(context).cart(uid).isEmpty()); assertNull(Preferences(context).requestId(uid))
            assertTrue(runCatching { repo.signIn("visitor@example.com", "TestOnly123!") }.isFailure)
            repo.signIn("carlos@coffeedev.demo", "CoffeeDev2026!")
            assertTrue(repo.member.value!!.owner)
            assertTrue(runCatching { repo.deleteLocalAccount() }.isFailure)
        } finally { repo.close() }
    }
    @Test fun deletionRemovesOrdersAndSurvivesReopen() = runBlocking {
        val repo = RepositoryFactory.create(context)
        repo.register("Visitor", "visitor@example.com", "TestOnly123!")
        val uid = repo.member.value!!.uid
        repo.signIn("carlos@coffeedev.demo", "CoffeeDev2026!"); repo.setAccess(uid, true)
        repo.placeOrder(mapOf("espresso" to 1), "owner", "keep")
        repo.signIn("visitor@example.com", "TestOnly123!")
        repo.placeOrder(mapOf("espresso" to 1), "visitor", "remove")
        repo.deleteLocalAccount(); repo.close()
        val reopened = RepositoryFactory.create(context)
        try {
            reopened.signIn("carlos@coffeedev.demo", "CoffeeDev2026!")
            assertEquals(listOf("keep"), reopened.orders.value.map { it.id })
            assertFalse(reopened.members.value.any { it.uid == uid })
        } finally { reopened.close() }
    }
    @Test fun alreadyMigratedDatabaseAlsoPurgesLegacyCopy() {
        LocalDatabase(context).use { it.putStrings(mapOf("users" to "[]")) }
        val old = context.getSharedPreferences("coffeedev_demo", 0)
        old.edit().putString("users", "obsolete personal copy").commit()
        LocalDatabase(context).use { assertEquals("[]", it.getString("users", null)) }
        assertTrue(old.all.isEmpty())
    }
    @Test fun validLegacyPasswordIsUpgradedAndOldPreferencesArePurged() = runBlocking {
        val password = "Legacy123!"; val salt = "old-salt"
        val hash = MessageDigest.getInstance("SHA-256").digest((salt+password).toByteArray()).joinToString("") { "%02x".format(it) }
        val user = JSONObject().put("uid", "legacy").put("name", "Legacy").put("email", "legacy@example.com").put("status", "pending").put("owner", false).put("salt",salt).put("hash",hash)
        val old = context.getSharedPreferences("coffeedev_demo", 0)
        old.edit().putString("users", JSONArray().put(user).toString()).commit()
        val repo = RepositoryFactory.create(context)
        try {
            assertTrue(old.all.isEmpty())
            assertTrue(runCatching { repo.signIn("legacy@example.com", "wrong") }.isFailure)
            repo.signIn("legacy@example.com", password)
            LocalDatabase(context).use {
                val upgraded = JSONArray(it.getString("users", "[]")).getJSONObject(0)
                assertEquals("pbkdf2-sha256", upgraded.getString("algorithm"))
                assertNotEquals(hash,upgraded.getString("hash"))
                assertTrue(PasswordHasher.verify(password, upgraded))
            }
        } finally { repo.close() }
    }
}
