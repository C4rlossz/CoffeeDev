package dev.coffeedev.data

import android.content.Context
import org.json.JSONObject

class Preferences(context: Context) {
    private val prefs = context.getSharedPreferences("coffeedev_settings", Context.MODE_PRIVATE)
    var language: String
        get() = prefs.getString("language", "pt") ?: "pt"
        set(value) { prefs.edit().putString("language", value).apply() }
    var theme: String
        get() = prefs.getString("theme", "system") ?: "system"
        set(value) { prefs.edit().putString("theme", value).apply() }
    fun removeAccount(uid: String) {
        prefs.edit().remove("request_$uid").remove("favorites_$uid").remove("cart_$uid").commit()
    }
    fun exportedSettings(uid: String): JSONObject = JSONObject().put("language", language).put("theme", theme)
        .put("favorites", org.json.JSONArray(favorites(uid).toList())).put("cart", JSONObject(cart(uid)))
        .put("pendingRequestId", requestId(uid) ?: JSONObject.NULL)
    fun requestId(uid: String): String? = prefs.getString("request_$uid", null)
    fun saveRequestId(uid: String, value: String?) { prefs.edit().putString("request_$uid", value).apply() }
    fun favorites(uid: String) = prefs.getStringSet("favorites_$uid", emptySet())!!.toSet()
    fun saveFavorites(uid: String, value: Set<String>) { prefs.edit().putStringSet("favorites_$uid", value).apply() }
    fun cart(uid: String): Map<String, Int> = runCatching {
        val json = JSONObject(prefs.getString("cart_$uid", "{}")!!)
        json.keys().asSequence().associateWith { json.getInt(it) }.filter { it.key in Catalog.ids && it.value in 1..10 }
    }.getOrDefault(emptyMap())
    fun saveCart(uid: String, value: Map<String, Int>) { prefs.edit().putString("cart_$uid", JSONObject(value).toString()).apply() }
}
