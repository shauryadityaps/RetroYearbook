package com.yearbook.retro.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.res.ResourcesCompat
import com.yearbook.retro.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateStampRenderer {

    /**
     * Renders vintage amber digital camera date stamp ('26 MM DD) onto a bitmap.
     */
    fun applyDateStamp(
        context: Context,
        source: Bitmap,
        date: Date = Date()
    ): Bitmap {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        // Format: '26 09 15 (2-digit year, 2-digit month, 2-digit day)
        val yearFormat = SimpleDateFormat("yy", Locale.US)
        val monthFormat = SimpleDateFormat("MM", Locale.US)
        val dayFormat = SimpleDateFormat("dd", Locale.US)

        val stampText = "'${yearFormat.format(date)} ${monthFormat.format(date)} ${dayFormat.format(date)}"

        // Scale font size proportionally to image width
        val textSize = (result.width * 0.038f).coerceIn(24f, 72f)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFE67E22") // DateStampAmber
            this.textSize = textSize
            try {
                typeface = ResourcesCompat.getFont(context, R.font.elegant_typewriter_bold)
            } catch (e: Exception) {
                // Fallback to default monospace/bold
                typeface = android.graphics.Typeface.MONOSPACE
            }
            // Amber glow / drop shadow
            setShadowLayer(textSize * 0.15f, 2f, 2f, Color.parseColor("#997A3800"))
        }

        val bounds = Rect()
        paint.getTextBounds(stampText, 0, stampText.length, bounds)

        val paddingRight = result.width * 0.05f
        val paddingBottom = result.height * 0.05f

        val x = result.width - bounds.width() - paddingRight
        val y = result.height - paddingBottom

        canvas.drawText(stampText, x, y, paint)
        return result
    }

    fun formatDateForHeader(dateMillis: Long): String {
        val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        return formatter.format(Date(dateMillis)).uppercase(Locale.US)
    }

    fun formatDateForBadge(dateMillis: Long): String {
        val formatter = SimpleDateFormat("MMM yyyy", Locale.US)
        return formatter.format(Date(dateMillis)).uppercase(Locale.US)
    }

    fun getTodayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }
}
