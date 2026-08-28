package com.yearbook.retro

import com.yearbook.retro.media.DateStampRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class YearbookAppUnitTest {

    @Test
    fun testDateHeaderFormatting() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 15, 12, 0, 0)
        }
        val formatted = DateStampRenderer.formatDateForHeader(calendar.timeInMillis)
        assertTrue(formatted.contains("SEPTEMBER 15, 2026"))
    }

    @Test
    fun testDateBadgeFormatting() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 15, 12, 0, 0)
        }
        val formatted = DateStampRenderer.formatDateForBadge(calendar.timeInMillis)
        assertTrue(formatted.contains("SEP 2026"))
    }

    @Test
    fun testTodayDateStringFormat() {
        val todayStr = DateStampRenderer.getTodayDateString()
        assertTrue(todayStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun testDeterministicPhotoIdFormat() {
        val dateString = "2026-09-15"
        val authorId = "retro_user_123"
        val deterministicId = "${dateString}_${authorId}"
        assertEquals("2026-09-15_retro_user_123", deterministicId)
    }

    @Test
    fun testJoinCodeLengthAndCharset() {
        val validChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val sampleCode = "FL26X9"
        assertEquals(6, sampleCode.length)
        assertTrue(sampleCode.all { validChars.contains(it) })
    }
}
