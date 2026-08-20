package com.novasphere.apexoverlay.ui.crosshair

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.novasphere.apexoverlay.ui.theme.ApexAccent
import com.novasphere.apexoverlay.ui.theme.ApexSurfaceElevated
import kotlin.math.abs

private const val TAP_MOVEMENT_THRESHOLD_PX = 12f

@Composable
fun QuickControlButton(
    onDrag: (dxPx: Float, dyPx: Float) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(ApexSurfaceElevated)
            .border(width = 1.5.dp, color = ApexAccent, shape = CircleShape)
            .pointerInput(Unit) {
                var totalMovement = 0f
                detectDragGestures(
                    onDragStart = { totalMovement = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalMovement += abs(dragAmount.x) + abs(dragAmount.y)
                        onDrag(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = {
                        if (totalMovement < TAP_MOVEMENT_THRESHOLD_PX) {
                            onTap()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "+", color = ApexAccent, style = MaterialTheme.typography.titleMedium)
    }
}
