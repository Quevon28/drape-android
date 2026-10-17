package com.drape.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.drape.app.core.designsystem.theme.DrapeThemeTokens

/**
 * Renders a real photographed or gallery-imported garment image via Coil 3 (`AsyncImage`),
 * with a GPU-cached shimmer skeleton during bitmap loading and a graceful error/silhouette
 * fallback if the image URI is missing or unreadable.
 */
@Composable
fun LiquidGlassGarmentImage(
    imageUri: String?,
    contentDescription: String,
    swatchColor: Color,
    modifier: Modifier = Modifier,
    fallbackIconSize: Dp = 44.dp
) {
    val glassColors = DrapeThemeTokens.glassColors
    var painterState by remember(imageUri) {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        swatchColor.copy(alpha = 0.30f),
                        glassColors.canvasSecondaryBackground.copy(alpha = 0.72f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Rounded.Checkroom,
                contentDescription = contentDescription,
                tint = glassColors.textSecondary.copy(alpha = 0.58f),
                modifier = Modifier.size(fallbackIconSize)
            )
        } else {
            if (painterState is AsyncImagePainter.State.Loading) {
                LiquidGlassShimmerBox(modifier = Modifier.fillMaxSize())
            }

            if (painterState is AsyncImagePainter.State.Error) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BrokenImage,
                        contentDescription = "Photo unavailable",
                        tint = glassColors.textMuted,
                        modifier = Modifier.size(fallbackIconSize * 0.75f)
                    )
                    Text(
                        text = "Silhouette Fallback",
                        style = MaterialTheme.typography.labelSmall,
                        color = glassColors.textMuted
                    )
                }
            }

            AsyncImage(
                model = imageUri,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                onState = { state -> painterState = state },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
