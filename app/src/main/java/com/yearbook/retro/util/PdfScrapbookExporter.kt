package com.yearbook.retro.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.yearbook.retro.data.model.PhotoEntry
import com.yearbook.retro.data.model.User
import com.yearbook.retro.data.model.Yearbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High-resolution on-device PDF Scrapbook Exporter for Retro Yearbook.
 * Generates an archival parchment-styled vintage book with cover, polaroid frames,
 * author details, captions, and date stamps.
 */
object PdfScrapbookExporter {

    // Standard A4 dimensions at 72 DPI (595 x 842 points)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    // Vintage Palette Colors
    private val COLOR_PARCHMENT = Color.rgb(247, 242, 234)
    private val COLOR_PARCHMENT_CARD = Color.rgb(253, 250, 245)
    private val COLOR_DARK_SEPIA = Color.rgb(44, 30, 20)
    private val COLOR_MUTED_SEPIA = Color.rgb(115, 95, 82)
    private val COLOR_SADDLE_LEATHER = Color.rgb(139, 69, 19)
    private val COLOR_GOLD_FOIL = Color.rgb(201, 150, 58)
    private val COLOR_DATE_AMBER = Color.rgb(217, 119, 6)
    private val COLOR_POLAROID_BG = Color.rgb(255, 253, 250)
    private val COLOR_POLAROID_BORDER = Color.rgb(222, 212, 198)

    suspend fun generateScrapbookPdf(
        context: Context,
        yearbook: Yearbook,
        members: List<User>,
        photos: List<PhotoEntry>,
        onProgress: (String) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            onProgress("Preparing scrapbook cover...")
            val pdfDoc = PdfDocument()
            var pageNumber = 1

            // 1. COVER PAGE
            val coverPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val coverPage = pdfDoc.startPage(coverPageInfo)
            drawCoverPage(coverPage.canvas, yearbook, members, photos.size)
            pdfDoc.finishPage(coverPage)
            pageNumber++

            // 2. SCRAPBOOK PHOTO PAGES (2 photos per page for large, high-res polaroids)
            val sortedPhotos = photos.sortedBy { it.timestamp }
            val photoChunks = sortedPhotos.chunked(2)
            val totalPhotoPages = photoChunks.size

            for ((chunkIndex, chunk) in photoChunks.withIndex()) {
                onProgress("Rendering memories: page ${chunkIndex + 1} of $totalPhotoPages...")
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                drawPhotoPage(
                    canvas = page.canvas,
                    photos = chunk,
                    pageIndex = chunkIndex + 1,
                    totalPages = totalPhotoPages,
                    yearbookTitle = yearbook.title
                )
                pdfDoc.finishPage(page)
                pageNumber++
            }

            // 3. WRITE TO STORAGE FILE
            onProgress("Sealing and saving PDF document...")
            val exportDir = File(context.cacheDir, "yearbook_exports").apply { mkdirs() }
            val sanitizedTitle = yearbook.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
            val pdfFile = File(exportDir, "${sanitizedTitle}_Scrapbook.pdf")

            FileOutputStream(pdfFile).use { outStream ->
                pdfDoc.writeTo(outStream)
            }
            pdfDoc.close()

            Result.success(pdfFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawCoverPage(
        canvas: Canvas,
        yearbook: Yearbook,
        members: List<User>,
        photoCount: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = COLOR_PARCHMENT
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Outer vintage border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = COLOR_GOLD_FOIL
        canvas.drawRect(24f, 24f, (PAGE_WIDTH - 24).toFloat(), (PAGE_HEIGHT - 24).toFloat(), paint)

        // Inner decorative border
        paint.strokeWidth = 1f
        paint.color = COLOR_SADDLE_LEATHER
        canvas.drawRect(28f, 28f, (PAGE_WIDTH - 28).toFloat(), (PAGE_HEIGHT - 28).toFloat(), paint)

        // Title Header Emblem
        paint.style = Paint.Style.FILL
        paint.color = COLOR_SADDLE_LEATHER
        val centerX = PAGE_WIDTH / 2f
        canvas.drawCircle(centerX, 130f, 32f, paint)

        paint.color = COLOR_GOLD_FOIL
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawCircle(centerX, 130f, 32f, paint)

        // Header Subtitle
        paint.style = Paint.Style.FILL
        paint.color = COLOR_MUTED_SEPIA
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("RETRO YEARBOOK • ARCHIVAL MEMORIES", centerX, 195f, paint)

        // Yearbook Title
        paint.color = COLOR_DARK_SEPIA
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        canvas.drawText(yearbook.title.ifBlank { "Untitled Yearbook" }, centerX, 245f, paint)

        // Gold divider line
        paint.color = COLOR_GOLD_FOIL
        paint.strokeWidth = 2f
        canvas.drawLine(centerX - 100f, 265f, centerX + 100f, 265f, paint)

        // Description / Notes
        paint.color = COLOR_MUTED_SEPIA
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val desc = yearbook.description.ifBlank { "A timeless collection of authentic shared moments." }
        canvas.drawText(desc, centerX, 295f, paint)

        // Date Range & Stats Card
        val cardRect = RectF(60f, 340f, (PAGE_WIDTH - 60).toFloat(), 530f)
        paint.color = COLOR_PARCHMENT_CARD
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(cardRect, 12f, 12f, paint)

        paint.color = COLOR_POLAROID_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(cardRect, 12f, 12f, paint)

        // Inside Card Information
        paint.style = Paint.Style.FILL
        paint.color = COLOR_SADDLE_LEATHER
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("ALBUM DETAILS & ARCHIVE STATS", centerX, 375f, paint)

        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        val createdDateStr = dateFormat.format(Date(yearbook.createdAt))
        val sealedDateStr = dateFormat.format(Date(if (yearbook.endDate > 0) yearbook.endDate else System.currentTimeMillis()))

        paint.color = COLOR_DARK_SEPIA
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        canvas.drawText("Timeline: $createdDateStr — $sealedDateStr", centerX, 410f, paint)
        canvas.drawText("Total Preserved Memories: $photoCount Photos", centerX, 435f, paint)
        canvas.drawText("Unique Join Code: ${yearbook.joinCode}", centerX, 460f, paint)

        // Contributors list
        val memberNames = members.mapNotNull { it.displayName.takeIf { name -> name.isNotBlank() } }
        val contributorsStr = if (memberNames.isNotEmpty()) {
            "Contributors: " + memberNames.joinToString(", ")
        } else {
            "Contributors: The Yearbook Collective"
        }
        paint.color = COLOR_MUTED_SEPIA
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)
        canvas.drawText(contributorsStr.take(75), centerX, 495f, paint)

        // Seal Graphic at Bottom
        paint.color = COLOR_DATE_AMBER
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("★ SEALED & PERMANENTLY PRESERVED ★", centerX, 680f, paint)

        paint.color = COLOR_MUTED_SEPIA
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("Generated on-device via Retro Yearbook Mobile Engine", centerX, 790f, paint)
    }

    private fun drawPhotoPage(
        canvas: Canvas,
        photos: List<PhotoEntry>,
        pageIndex: Int,
        totalPages: Int,
        yearbookTitle: String
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Page Background
        paint.color = COLOR_PARCHMENT
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Delicate outer border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = COLOR_POLAROID_BORDER
        canvas.drawRect(20f, 20f, (PAGE_WIDTH - 20).toFloat(), (PAGE_HEIGHT - 20).toFloat(), paint)

        // Header
        paint.style = Paint.Style.FILL
        paint.color = COLOR_MUTED_SEPIA
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(yearbookTitle.uppercase(Locale.US), 35f, 40f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("MEMORY SCRAPBOOK", (PAGE_WIDTH - 35).toFloat(), 40f, paint)

        // Render Polaroid Frames
        val cardWidth = 490f
        val cardHeight = 330f
        val startX = (PAGE_WIDTH - cardWidth) / 2f

        val yPositions = if (photos.size == 1) {
            listOf(220f)
        } else {
            listOf(60f, 420f)
        }

        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)

        for (i in photos.indices) {
            val photo = photos[i]
            val cardY = yPositions[i]
            val cardRect = RectF(startX, cardY, startX + cardWidth, cardY + cardHeight)

            // 1. Polaroid White Canvas Background
            paint.color = COLOR_POLAROID_BG
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(cardRect, 8f, 8f, paint)

            paint.color = COLOR_POLAROID_BORDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(cardRect, 8f, 8f, paint)

            // 2. Photo Area (Keep aspect ratio, center fit)
            val imgMargin = 16f
            val imgTop = cardY + imgMargin
            val imgLeft = startX + imgMargin
            val imgWidth = cardWidth - (imgMargin * 2)
            val imgHeight = 230f
            val imgRect = RectF(imgLeft, imgTop, imgLeft + imgWidth, imgTop + imgHeight)

            // Dark photo placeholder base
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(238, 232, 222)
            canvas.drawRect(imgRect, paint)

            // Download and draw actual bitmap
            val bitmap = downloadBitmap(photo.photoUrl)
            if (bitmap != null) {
                drawScaledBitmap(canvas, bitmap, imgRect)
            }

            // Photo frame border
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = COLOR_POLAROID_BORDER
            canvas.drawRect(imgRect, paint)

            // 3. Metadata & Caption below photo
            val textY = imgTop + imgHeight + 24f

            // Date Stamp (Amber Vintage)
            paint.style = Paint.Style.FILL
            paint.color = COLOR_DATE_AMBER
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            paint.textAlign = Paint.Align.LEFT
            val dateText = dateFormat.format(Date(photo.timestamp))
            canvas.drawText(dateText, imgLeft + 4f, textY, paint)

            // Author Name
            paint.color = COLOR_SADDLE_LEATHER
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.RIGHT
            val authorText = "Captured by ${photo.authorName.ifBlank { "Friend" }}"
            canvas.drawText(authorText, imgLeft + imgWidth - 4f, textY, paint)

            // Caption
            val captionText = photo.caption.ifBlank { "A golden memory." }
            paint.color = COLOR_DARK_SEPIA
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("\"${captionText.take(65)}\"", imgLeft + 4f, textY + 22f, paint)
        }

        // Footer with page numbering
        paint.color = COLOR_MUTED_SEPIA
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Page $pageIndex of $totalPages", (PAGE_WIDTH / 2f), (PAGE_HEIGHT - 25).toFloat(), paint)
    }

    private fun drawScaledBitmap(canvas: Canvas, bitmap: Bitmap, destRect: RectF) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val srcWidth = bitmap.width.toFloat()
        val srcHeight = bitmap.height.toFloat()

        // Calculate aspect fit inside destRect
        val scale = Math.min(destRect.width() / srcWidth, destRect.height() / srcHeight)
        val scaledWidth = srcWidth * scale
        val scaledHeight = srcHeight * scale

        val left = destRect.left + (destRect.width() - scaledWidth) / 2f
        val top = destRect.top + (destRect.height() - scaledHeight) / 2f
        val targetRect = RectF(left, top, left + scaledWidth, top + scaledHeight)

        canvas.drawBitmap(bitmap, null, targetRect, paint)
    }

    private fun downloadBitmap(urlStr: String): Bitmap? {
        if (urlStr.isBlank()) return null
        return try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 10000
            connection.doInput = true
            connection.connect()

            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Share or open the generated PDF with Android's system Sharesheet.
     */
    fun sharePdf(context: Context, pdfFile: File, title: String = "Yearbook PDF Scrapbook") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Here is our completed Retro Yearbook PDF scrapbook!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Save or Share Yearbook PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
