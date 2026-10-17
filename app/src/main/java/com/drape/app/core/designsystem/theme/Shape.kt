package com.drape.app.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val DrapeShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

/**
 * Semantic component shapes for Liquid Glass cards, floating capsules, and bottom sheets.
 */
@Immutable
data class DrapeComponentShapes(
    val pill: Shape = CircleShape,
    val chip: Shape = RoundedCornerShape(50),
    val searchBar: Shape = RoundedCornerShape(22.dp),
    val garmentCard: Shape = RoundedCornerShape(24.dp),
    val garmentInnerImage: Shape = RoundedCornerShape(18.dp),
    val statCard: Shape = RoundedCornerShape(22.dp),
    val floatingNavBar: Shape = RoundedCornerShape(34.dp),
    val floatingNavIndicator: Shape = RoundedCornerShape(28.dp),
    val modalBottomSheet: Shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
)
