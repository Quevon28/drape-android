package com.drape.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.theme.DrapeThemeTokens

/**
 * Reusable Liquid Glass sheet container for garment details, outfit drawers, and AI explanations.
 */
@Composable
fun LiquidGlassSheetContainer(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onDismiss: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassColors = DrapeThemeTokens.glassColors
    val shapes = DrapeThemeTokens.componentShapes

    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = shapes.modalBottomSheet,
        elevation = GlassElevation.Floating
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pill drag handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(glassColors.specularRimTop)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = glassColors.textPrimary
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textSecondary
                        )
                    }
                }
                if (onDismiss != null) {
                    LiquidGlassIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close sheet",
                        onClick = onDismiss,
                        size = 36.dp
                    )
                }
            }

            content()
        }
    }
}
