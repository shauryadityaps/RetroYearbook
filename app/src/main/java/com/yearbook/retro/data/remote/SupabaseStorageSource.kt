package com.yearbook.retro.data.remote

import android.graphics.Bitmap
import com.yearbook.retro.media.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Native Supabase Storage client.
 * Uploads compressed high-quality WebP polaroid images directly to Supabase Storage
 * via standard REST API and generates permanent public CDN URLs for cross-device sharing.
 */
class SupabaseStorageSource(
    private val projectUrl: String = "https://vqahvognmtqsojeoxacs.supabase.co",
    private val apiKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZxYWh2b2dubXRxc29qZW94YWNzIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4Nzc1NjI1MCwiZXhwIjoyMTAzMzMyMjUwfQ._GUvt1ftfYbEea538uDTMs-IIkRvG6iqzerY-VExInE",
    private val bucketName: String = "yearbooks"
) {

    suspend fun uploadPhoto(
        yearbookId: String,
        photoDocId: String,
        bitmap: Bitmap
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bytes = ImageCompressor.bitmapToWebPBytes(bitmap)
            val path = "yearbooks/$yearbookId/$photoDocId.webp"
            val uploadEndpoint = "$projectUrl/storage/v1/object/$bucketName/$path"

            val url = URL(uploadEndpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("apikey", apiKey)
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Content-Type", "image/webp")
                setRequestProperty("x-upsert", "true") // allows overwriting/updating existing date photos
                setFixedLengthStreamingMode(bytes.size)
                connectTimeout = 25000
                readTimeout = 25000
            }

            connection.outputStream.use { os ->
                os.write(bytes)
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val publicUrl = "$projectUrl/storage/v1/object/public/$bucketName/$path"
                Result.success(publicUrl)
            } else {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    ?: "Supabase upload failed with HTTP $responseCode"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePhoto(
        yearbookId: String,
        photoDocId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val path = "yearbooks/$yearbookId/$photoDocId.webp"
            val deleteEndpoint = "$projectUrl/storage/v1/object/$bucketName/$path"
            val url = URL(deleteEndpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("apikey", apiKey)
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 10000
                readTimeout = 10000
            }
            connection.responseCode
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
