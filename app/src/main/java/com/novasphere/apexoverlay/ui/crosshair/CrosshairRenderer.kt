package com.novasphere.apexoverlay.ui.crosshair

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CrosshairCanvas(
    config: CrosshairConfig,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseColor = config.color.copy(alpha = config.opacity)
        val sizePx = config.sizeDp.dp.toPx()
        val thicknessPx = config.thicknessDp.dp.toPx()

        if (config.glowEnabled) {
            drawCrosshairShape(
                shape = config.shape,
                center = center,
                sizePx = sizePx * 1.6f,
                thicknessPx = thicknessPx * 2.6f,
                color = baseColor.copy(alpha = baseColor.alpha * 0.18f)
            )
            drawCrosshairShape(
                shape = config.shape,
                center = center,
                sizePx = sizePx * 1.25f,
                thicknessPx = thicknessPx * 1.7f,
                color = baseColor.copy(alpha = baseColor.alpha * 0.3f)
            )
        }

        drawCrosshairShape(
            shape = config.shape,
            center = center,
            sizePx = sizePx,
            thicknessPx = thicknessPx,
            color = baseColor
        )
    }
}

private fun DrawScope.drawCrosshairShape(
    shape: CrosshairShape,
    center: Offset,
    sizePx: Float,
    thicknessPx: Float,
    color: Color
) {
    val armLength = sizePx / 2f
    val gap = sizePx * 0.22f

    when (shape) {
        CrosshairShape.CROSS -> {
            drawLine(color = color, start = Offset(center.x, center.y - armLength), end = Offset(center.x, center.y - gap), strokeWidth = thicknessPx, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(center.x, center.y + gap), end = Offset(center.x, center.y + armLength), strokeWidth = thicknessPx, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(center.x - armLength, center.y), end = Offset(center.x - gap, center.y), strokeWidth = thicknessPx, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(center.x + gap, center.y), end = Offset(center.x + armLength, center.y), strokeWidth = thicknessPx, cap = StrokeCap.Round)
        }
        CrosshairShape.PLUS -> {
            drawLine(color = color, start = Offset(center.x, center.y - armLength), end = Offset(center.x, center.y + armLength), strokeWidth = thicknessPx, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(center.x - armLength, center.y), end = Offset(center.x + armLength, center.y), strokeWidth = thicknessPx, cap = StrokeCap.Round)
        }
        CrosshairShape.DOT -> {
            drawCircle(color = color, radius = thicknessPx * 1.4f, center = center)
        }
        CrosshairShape.CIRCLE -> {
            drawCircle(color = color, radius = armLength, center = center, style = Stroke(width = thicknessPx))
        }
        CrosshairShape.CIRCLE_DOT -> {
            drawCircle(color = color, radius = armLength, center = center, style = Stroke(width = thicknessPx))
            drawCircle(color = color, radius = thicknessPx * 1.2f, center = center)
        }
        CrosshairShape.X_SHAPE -> {
            val dx = armLength * 0.70710678f
            drawLine(color = color, start = Offset(center.x - dx, center.y - dx), end = Offset(center.x - gap * 0.7f, center.y - gap * 0.7f), strokeWidth = thicknessPx, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(center.x + gap * 0.7f, center.y + gap * 0.7f), end = Offset(center.x + dx, center.y + dx), strokeWidth = thicknessPx, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(center.x - dx, center.y + dx), end = Offset(center.x - gap * 0.7f, center.y + gap * 0.7f), strokeWidth = thicknessPx, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(center.x + gap * 0.7f, center.y - gap * 0.7f), end = Offset(center.x + dx, center.y - dx), strokeWidth = thicknessPx, cap = StrokeCap.Round)
        }
    }
}
