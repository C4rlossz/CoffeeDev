package dev.coffeedev.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import org.json.JSONObject

/** Local demonstration accounts only. Never a replacement for a production identity service. */
internal object PasswordHasher {
    const val ITERATIONS = 210_000
    fun newSalt(): String = Base64.getEncoder().encodeToString(ByteArray(32).also { SecureRandom().nextBytes(it) })
    fun hash(password: String, salt: String, iterations: Int = ITERATIONS): String {
        require(iterations in 100_000..1_000_000)
        val spec = PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), iterations, 256)
        return try { Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded) }
        finally { spec.clearPassword() }
    }
    fun verify(password: String, stored: JSONObject): Boolean {
        val actual = if (stored.optString("algorithm") == "pbkdf2-sha256") hash(password, stored.getString("salt"), stored.getInt("iterations"))
        else MessageDigest.getInstance("SHA-256").digest((stored.getString("salt") + password).toByteArray()).joinToString("") { "%02x".format(it) }
        return MessageDigest.isEqual(actual.toByteArray(), stored.getString("hash").toByteArray())
    }
}
