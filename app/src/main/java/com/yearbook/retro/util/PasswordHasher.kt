package com.yearbook.retro.util

import java.security.MessageDigest

/**
 * Cryptographically hashes and verifies passwords using SHA-256 with dedicated application salt.
 */
object PasswordHasher {

    private const val SALT = "retro_yearbook_salt_2026_secure_"

    /**
     * Compute SHA-256 hash of salted password.
     */
    fun hash(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val saltedBytes = (SALT + password.trim()).toByteArray(Charsets.UTF_8)
        val hashBytes = messageDigest.digest(saltedBytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verify whether a raw password matches the stored SHA-256 hash.
     */
    fun verify(password: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return false
        val computed = hash(password)
        return computed.equals(storedHash.trim(), ignoreCase = true)
    }

    /**
     * Extract password hash from photoUrl field if encoded.
     */
    fun extractHash(encodedString: String): String {
        if (encodedString.startsWith("pwd:")) {
            return encodedString.removePrefix("pwd:").substringBefore("|")
        }
        return encodedString
    }

    /**
     * Encode password hash into photoUrl field.
     */
    fun encodeHash(hash: String, photoUrl: String = ""): String {
        return if (photoUrl.isBlank()) "pwd:$hash" else "pwd:$hash|$photoUrl"
    }

    /**
     * Extract pure photo URL excluding password hash.
     */
    fun extractPhotoUrl(encodedString: String): String {
        if (encodedString.startsWith("pwd:")) {
            val parts = encodedString.removePrefix("pwd:").split("|", limit = 2)
            return if (parts.size > 1) parts[1] else ""
        }
        return encodedString
    }
}
