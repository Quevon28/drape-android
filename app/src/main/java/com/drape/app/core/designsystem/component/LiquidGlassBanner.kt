package com.drape.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.theme.DrapeThemeTokens

/**
 * Subtle translucent status banner for offline readiness, sync states, or AI service notices.
 */
@Composable
fun LiquidGlassBanner(
    message: String,
    modifier: Modifier = Modifier,
    badgeLabel: String? = null,
    indicatorColor: Color = DrapeThemeTokens.glassColors.statusSuccess
) {
    val glassColors = DrapeThemeTokens.glassColors
    val editorialTypography = DrapeThemeTokens.editorialTypography

    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = GlassElevation.Subtle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = glassColors.textSecondary,
                modifier = Modifier.weight(1f)
            )
            if (!badgeLabel.isNullOrBlank()) {
                Text(
                    text = badgeLabel.uppercase(),
                    style = editorialTypography.eyebrowUpper,
                    color = glassColors.accentPrimary
                )
            }
        }
    }
}
