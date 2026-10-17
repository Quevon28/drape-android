package com.drape.app.core.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.drape.app.core.model.GarmentItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * Extracts real visual color, luminance, warmth, and compact base64 thumbnail features
 * directly from the user's photographed or gallery-imported garment image file (`localImageUri`),
 * with a deterministic fallback to the garment's swatch color when no photo file exists or in unit tests.
 */
object GarmentPhotoVisionAnalyzer {

    suspend fun extractSignature(garment: GarmentItem): GarmentVisualSignature =
        withContext(Dispatchers.Default) {
            extractSignatureSync(garment)
        }

    fun extractSignatureSync(garment: GarmentItem): GarmentVisualSignature {
        val localUri = garment.localImageUri
        if (!localUri.isNullOrBlank()) {
            val photoSignature = runCatching {
                analyzeLocalPhotoFile(garment.id, localUri)
            }.getOrNull()
            if (photoSignature != null) {
                return photoSignature
            }
        }
        return computeFromHex(
            garmentId = garment.id,
            hexCode = garment.swatch.hexCode,
            hasActualPhoto = !localUri.isNullOrBlank(),
            thumbnailBase64 = null
        )
    }

    private fun analyzeLocalPhotoFile(
        garmentId: String,
        localUriString: String
    ): GarmentVisualSignature? {
        val path = runCatching { Uri.parse(localUriString).path }.getOrNull()
            ?: localUriString.removePrefix("file://")
        val file = File(path)
        if (!file.exists() || !file.canRead()) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = 8
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
        val thumb = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
        if (thumb != bitmap) {
            bitmap.recycle()
        }

        // Sample the central 50% region where the garment is framed
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var count = 0L
        val startX = thumb.width / 4
        val endX = (thumb.width * 3) / 4
        val startY = thumb.height / 4
        val endY = (thumb.height * 3) / 4

        for (y in startY until endY) {
            for (x in startX until endX) {
                val pixel = thumb.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                rSum += r
                gSum += g
                bSum += b
                count++
            }
        }

        val avgR = if (count > 0) (rSum / count).toInt() else 128
        val avgG = if (count > 0) (gSum / count).toInt() else 128
        val avgB = if (count > 0) (bSum / count).toInt() else 128
        val dominantHex = String.format(Locale.US, "#%02X%02X%02X", avgR, avgG, avgB)

        val encodedThumb = runCatching {
            ByteArrayOutputStream().use { bos ->
                thumb.compress(Bitmap.CompressFormat.JPEG, 70, bos)
                Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
            }
        }.getOrNull()

        thumb.recycle()

        return computeFromRgb(
            garmentId = garmentId,
            r = avgR,
            g = avgG,
            b = avgB,
            dominantHex = dominantHex,
            hasActualPhoto = true,
            thumbnailBase64 = encodedThumb
        )
    }

    fun computeFromHex(
        garmentId: String,
        hexCode: String,
        hasActualPhoto: Boolean = false,
        thumbnailBase64: String? = null
    ): GarmentVisualSignature {
        val clean = hexCode.trim().removePrefix("#")
        val parsed = clean.toLongOrNull(16) ?: 0x141619L
        val r = ((parsed shr 16) and 0xFF).toInt()
        val g = ((parsed shr 8) and 0xFF).toInt()
        val b = (parsed and 0xFF).toInt()
        return computeFromRgb(
            garmentId = garmentId,
            r = r,
            g = g,
            b = b,
            dominantHex = "#${clean.uppercase(Locale.US)}",
            hasActualPhoto = hasActualPhoto,
            thumbnailBase64 = thumbnailBase64
        )
    }

    private fun computeFromRgb(
        garmentId: String,
        r: Int,
        g: Int,
        b: Int,
        dominantHex: String,
        hasActualPhoto: Boolean,
        thumbnailBase64: String?
    ): GarmentVisualSignature {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        val luminance = (0.2126f * rf + 0.7152f * gf + 0.0722f * bf).coerceIn(0f, 1f)
        val warmth = ((rf - bf) + (gf - bf) * 0.25f).coerceIn(-1f, 1f)
        val maxC = max(rf, max(gf, bf))
        val minC = min(rf, min(gf, bf))
        val saturation = if (maxC == 0f) 0f else ((maxC - minC) / maxC).coerceIn(0f, 1f)

        return GarmentVisualSignature(
            garmentId = garmentId,
            dominantHex = dominantHex,
            relativeLuminance = luminance,
            warmthScore = warmth,
            saturationScore = saturation,
            hasActualPhoto = hasActualPhoto,
            thumbnailBase64 = thumbnailBase64
        )
    }
}
