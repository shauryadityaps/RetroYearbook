package com.yearbook.retro.data.model

data class Yearbook(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val joinCode: String = "", // 6-character uppercase alphanumeric e.g. "FL26X9"
    val coverPhotoUrl: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000), // Default 90 days
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAtMs: Long = 0L,
    val retentionDays: Int = 30, // 30-day cloud retention grace period
    val totalMemories: Int = 0
) {
    /**
     * An album is considered sealed/completed if explicitly sealed/archived or the end date has passed.
     */
    val isAlbumSealed: Boolean
        get() = isCompleted || isArchived || (endDate > 1000000000000L && System.currentTimeMillis() >= endDate)

    /**
     * Calculates the timestamp when the cloud storage grace period expires.
     */
    fun getRetentionExpiryMs(): Long {
        val baseTime = when {
            completedAtMs > 1000000000000L -> completedAtMs
            endDate > 1000000000000L && endDate <= System.currentTimeMillis() -> endDate
            createdAt > 1000000000000L -> createdAt
            else -> System.currentTimeMillis()
        }
        return baseTime + (retentionDays.toLong() * 24L * 60L * 60L * 1000L)
    }

    /**
     * Number of whole days remaining until cloud assets are permanently cleared.
     */
    fun getDaysUntilDeletion(): Long {
        val expiryMs = getRetentionExpiryMs()
        val remainingMs = expiryMs - System.currentTimeMillis()
        return if (remainingMs > 0) {
            (remainingMs / (24L * 60L * 60L * 1000L)).coerceAtLeast(1)
        } else {
            0
        }
    }
}
