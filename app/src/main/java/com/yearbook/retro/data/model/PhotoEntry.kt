package com.yearbook.retro.data.model

data class PhotoEntry(
    val id: String = "", // deterministic "{dateString}_{authorId}"
    val yearbookId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val photoUrl: String = "",
    val dateString: String = "", // e.g. "2026-09-15"
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
