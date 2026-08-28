package com.yearbook.retro.data.model

enum class DailyDropStatus {
    PENDING,    // User has not yet dropped today's photo
    COMPLETED,  // User dropped today's photo (green wax seal stamp)
    ENDED       // Yearbook end date has passed
}
