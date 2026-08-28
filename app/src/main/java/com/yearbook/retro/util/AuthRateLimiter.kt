package com.yearbook.retro.util

/**
 * Client-side rate limiter for authentication and account registration attempts.
 * Limits users to max [MAX_ATTEMPTS] attempts per [WINDOW_MILLIS] window.
 * Enforces a progressive cooldown period when exceeded.
 */
object AuthRateLimiter {

    private const val MAX_ATTEMPTS = 4
    private const val WINDOW_MILLIS = 60_000L // 1 minute
    private const val COOLDOWN_MILLIS = 60_000L // 60 seconds lockout

    private val attemptTimestamps = mutableListOf<Long>()
    private var cooldownUntil: Long = 0L

    sealed class RateLimitResult {
        object Allowed : RateLimitResult()
        data class RateLimited(val remainingSeconds: Int) : RateLimitResult()
    }

    @Synchronized
    fun checkRateLimit(): RateLimitResult {
        val now = System.currentTimeMillis()

        if (now < cooldownUntil) {
            val remaining = (((cooldownUntil - now) / 1000) + 1).toInt()
            return RateLimitResult.RateLimited(remaining)
        }

        // Clean out timestamps older than the window
        attemptTimestamps.removeAll { timestamp -> now - timestamp > WINDOW_MILLIS }

        if (attemptTimestamps.size >= MAX_ATTEMPTS) {
            cooldownUntil = now + COOLDOWN_MILLIS
            return RateLimitResult.RateLimited((COOLDOWN_MILLIS / 1000).toInt())
        }

        return RateLimitResult.Allowed
    }

    @Synchronized
    fun recordAttempt() {
        val now = System.currentTimeMillis()
        attemptTimestamps.add(now)
    }

    @Synchronized
    fun reset() {
        attemptTimestamps.clear()
        cooldownUntil = 0L
    }
}
