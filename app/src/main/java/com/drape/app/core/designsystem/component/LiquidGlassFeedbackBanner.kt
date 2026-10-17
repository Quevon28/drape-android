package com.drape.app.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.drape.app.core.designsystem.theme.DrapeThemeTokens

@Immutable
enum class FeedbackSeverity {
    SUCCESS,
    INFO,
    WARNING,
    ERROR
}

@Immutable
data class DrapeUiFeedbackMessage(
    val id: String = "feedback_${System.currentTimeMillis()}",
    val title: String,
    val message: String,
    val severity: FeedbackSeverity = FeedbackSeverity.INFO,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

/**
 * Accessible, spring-animated Liquid Glass feedback banner for action confirmations,
 * input validation errors, and recoverable failure states.
 * Announces updates to TalkBack via `liveRegion = LiveRegionMode.Polite`.
 */
@Composable
fun LiquidGlassFeedbackBanner(
    feedback: DrapeUiFeedbackMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = DrapeThemeTokens.glassColors
    val editorialTypography = DrapeThemeTokens.editorialTypography

    AnimatedVisibility(
        visible = feedback != null,
        enter = slideInVertically(animationSpec = spring()) { -it / 2 } + fadeIn(animationSpec = spring()),
        exit = slideOutVertically(animationSpec = spring()) { -it / 2 } + fadeOut(animationSpec = spring()),
        modifier = modifier
    ) {
        if (feedback != null) {
            val accentColor = when (feedback.severity) {
                FeedbackSeverity.SUCCESS -> glassColors.statusSuccess
                FeedbackSeverity.INFO -> glassColors.accentPrimary
                FeedbackSeverity.WARNING -> glassColors.statusWarning
                FeedbackSeverity.ERROR -> glassColors.statusDanger
            }
            val icon = when (feedback.severity) {
                FeedbackSeverity.SUCCESS -> Icons.Rounded.CheckCircle
                FeedbackSeverity.INFO -> Icons.Rounded.Info
                FeedbackSeverity.WARNING -> Icons.Rounded.WarningAmber
                FeedbackSeverity.ERROR -> Icons.Rounded.ErrorOutline
            }

            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    },
                shape = RoundedCornerShape(22.dp),
                elevation = GlassElevation.Floating,
                isSelected = feedback.severity == FeedbackSeverity.ERROR || feedback.severity == FeedbackSeverity.WARNING
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = feedback.severity.name,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = feedback.title.uppercase(),
                            style = editorialTypography.eyebrowUpper,
                            color = accentColor
                        )
                        Text(
                            text = feedback.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textPrimary
                        )
                    }

                    if (!feedback.actionLabel.isNullOrBlank() && feedback.onAction != null) {
                        LiquidGlassSecondaryButton(
                            text = feedback.actionLabel,
                            onClick = feedback.onAction
                        )
                    }

                    LiquidGlassIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Dismiss notification",
                        onClick = onDismiss,
                        size = 32.dp
                    )
                }
            }
        }
    }
}
