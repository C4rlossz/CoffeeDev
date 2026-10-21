package dev.coffeedev.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Device-only SQLite database. Snapshots keep account/order writes transactional. */
class LocalDatabase(context: Context) : SQLiteOpenHelper(context, NAME, null, 1) {
    init {
        val previous = context.getSharedPreferences("coffeedev_demo", Context.MODE_PRIVATE)
        if (getString("migrated", null) == null) {
            val values = listOf("users", "orders", "menu", "session").associateWith { previous.getString(it, null) }.toMutableMap()
            values["migrated"] = "1"
            putStrings(values)
        }
        // Also remove the legacy copy left by version 1.1 after its completed migration.
        previous.edit().clear().commit()
    }
    override fun onConfigure(db: SQLiteDatabase) {
        db.rawQuery("PRAGMA secure_delete=ON", null).use { cursor ->
            check(cursor.moveToFirst() && cursor.getInt(0) == 1)
        }
    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE local_state (entry_key TEXT PRIMARY KEY NOT NULL, entry_value TEXT NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    fun getString(key: String, fallback: String?): String? = readableDatabase.query(
        "local_state", arrayOf("entry_value"), "entry_key = ?", arrayOf(key), null, null, null
    ).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else fallback }
    fun putStrings(values: Map<String, String?>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            values.forEach { (key, value) ->
                if (value == null) db.delete("local_state", "entry_key = ?", arrayOf(key))
                else db.insertWithOnConflict("local_state", null, ContentValues().apply {
                    put("entry_key", key); put("entry_value", value)
                }, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
    companion object { const val NAME = "coffeedev-local.db" }
}
