package com.novasphere.apexoverlay.ui.crosshair

import androidx.compose.ui.graphics.Color
import com.novasphere.apexoverlay.ui.theme.ApexAccent

enum class CrosshairShape {
    CROSS,
    PLUS,
    DOT,
    CIRCLE,
    CIRCLE_DOT,
    X_SHAPE
}

data class CrosshairConfig(
    val shape: CrosshairShape = CrosshairShape.CROSS,
    val color: Color = ApexAccent,
    val sizeDp: Float = 28f,
    val thicknessDp: Float = 3f,
    val opacity: Float = 1f,
    val glowEnabled: Boolean = true,
    val overlayEnabled: Boolean = false
)

val crosshairColorOptions: List<Color> = listOf(
    ApexAccent,
    Color(0xFFFF3B5C),
    Color(0xFF39FF88),
    Color(0xFFFFC93C),
    Color(0xFFB388FF),
    Color(0xFFFFFFFF)
)

val crosshairSizeRange: ClosedFloatingPointRange<Float> = 12f..64f
val crosshairThicknessRange: ClosedFloatingPointRange<Float> = 1f..10f
