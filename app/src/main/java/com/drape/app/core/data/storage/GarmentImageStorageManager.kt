package com.drape.app.core.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.drape.app.core.security.DrapeSecurityPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Manages app-private garment image files captured via CameraX or imported via Android Photo Picker.
 * Enforces canonical path-traversal checks via `DrapeSecurityPolicy.isSafeAppPrivatePath`
 * and downscales high-resolution bitmaps to 1440px max edge on Dispatchers.IO.
 */
@Singleton
class GarmentImageStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val garmentsDir: File
        get() = File(context.filesDir, "garments").apply {
            if (!exists()) mkdirs()
        }

    private val cameraCacheDir: File
        get() = File(context.cacheDir, "camera_captures").apply {
            if (!exists()) mkdirs()
        }

    fun createTemporaryCameraCaptureFile(): File {
        val safeName = DrapeSecurityPolicy.sanitizeFileName("capture_${UUID.randomUUID()}.jpg")
        return File(cameraCacheDir, safeName)
    }

    /**
     * Copies and optimizes a picked `content://` or `file://` URI into app-private `filesDir/garments/`.
     * Returns the persistent `file://` URI string, or null if decoding failed or image dimensions were invalid.
     */
    suspend fun importAndOptimizeImage(sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return@runCatching null
            }

            val sampleSize = calculateInSampleSize(
                width = boundsOptions.outWidth,
                height = boundsOptions.outHeight,
                maxEdgePx = MAX_GARMENT_IMAGE_EDGE_PX
            )

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decodedBitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@runCatching null

            val safeFileName = DrapeSecurityPolicy.sanitizeFileName("garment_${UUID.randomUUID()}.jpg")
            val destFile = File(garmentsDir, safeFileName)
            if (!DrapeSecurityPolicy.isSafeAppPrivatePath(destFile.canonicalPath, garmentsDir.canonicalPath)) {
                decodedBitmap.recycle()
                return@runCatching null
            }

            FileOutputStream(destFile).use { out ->
                decodedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                out.flush()
            }
            decodedBitmap.recycle()

            Uri.fromFile(destFile).toString()
        }.getOrNull()
    }

    /**
     * Finalizes a CameraX capture file into persistent app-private garment storage.
     */
    suspend fun persistCameraCaptureFile(tempCaptureFile: File): String? = withContext(Dispatchers.IO) {
        if (!tempCaptureFile.exists()) return@withContext null
        val savedUri = importAndOptimizeImage(Uri.fromFile(tempCaptureFile))
        if (DrapeSecurityPolicy.isSafeAppPrivatePath(tempCaptureFile.canonicalPath, cameraCacheDir.canonicalPath)) {
            runCatching { tempCaptureFile.delete() }
        }
        savedUri
    }

    /**
     * Deletes an app-private garment photo only if its canonical path resides inside `filesDir/garments/`.
     */
    suspend fun deleteLocalImageIfOwned(localImageUri: String?): Boolean = withContext(Dispatchers.IO) {
        if (localImageUri.isNullOrBlank()) return@withContext false
        runCatching {
            val uri = Uri.parse(localImageUri)
            val path = uri.path ?: return@runCatching false
            val file = File(path)
            if (file.exists() &&
                DrapeSecurityPolicy.isSafeAppPrivatePath(file.canonicalPath, garmentsDir.canonicalPath)
            ) {
                file.delete()
            } else {
                false
            }
        }.getOrDefault(false)
    }

    companion object {
        const val MAX_GARMENT_IMAGE_EDGE_PX = 1440
        const val JPEG_QUALITY = 88

        fun calculateInSampleSize(width: Int, height: Int, maxEdgePx: Int): Int {
            if (width <= 0 || height <= 0) return 1
            var inSampleSize = 1
            val longestEdge = max(width, height)
            if (longestEdge > maxEdgePx) {
                val halfEdge = longestEdge / 2
                while ((halfEdge / inSampleSize) >= maxEdgePx) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }
    }
}
