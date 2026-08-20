package com.novasphere.apexoverlay.ui.crosshair

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.novasphere.apexoverlay.overlay.OverlayDiagnostics
import com.novasphere.apexoverlay.ui.theme.ApexAccent
import com.novasphere.apexoverlay.ui.theme.ApexBorder
import com.novasphere.apexoverlay.ui.theme.ApexSurfaceElevated
import com.novasphere.apexoverlay.ui.theme.ApexTextSecondary

@Composable
fun QuickControlsPanel(
    config: CrosshairConfig,
    onConfigChange: (CrosshairConfig) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ApexSurfaceElevated)
            .border(width = 1.dp, color = ApexAccent, shape = RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "ApexOverlay Controls", style = MaterialTheme.typography.labelSmall)
            TextButton(onClick = onClose) {
                Text(text = "✕", color = ApexTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "Shape", style = MaterialTheme.typography.labelSmall, color = ApexTextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            crosshairPresets.forEach { preset ->
                val isSelected = preset.shape == config.shape
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ApexAccent.copy(alpha = 0.15f) else ApexSurfaceElevated)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) ApexAccent else ApexBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            OverlayDiagnostics.log(context, "UI Quick Controls changed shape=${preset.shape}")
                            onConfigChange(config.copy(shape = preset.shape))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CrosshairCanvas(
                        config = config.copy(
                            shape = preset.shape,
                            sizeDp = 14f,
                            thicknessDp = 2f,
                            glowEnabled = false
                        ),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Color", style = MaterialTheme.typography.labelSmall, color = ApexTextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            crosshairColorOptions.forEach { color ->
                val isSelected = color == config.color
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White else ApexBorder,
                            shape = CircleShape
                        )
                        .clickable {
                            OverlayDiagnostics.log(context, "UI Quick Controls changed color")
                            onConfigChange(config.copy(color = color))
                        }
                )
            }
        }

        QuickSlider(
            label = "Size",
            valueLabel = "${config.sizeDp.toInt()} dp",
            value = config.sizeDp,
            range = crosshairSizeRange,
            onValueChange = { onConfigChange(config.copy(sizeDp = it)) },
            onValueChangeFinished = {
                OverlayDiagnostics.log(context, "UI Quick Controls changed size=${config.sizeDp.toInt()}")
            }
        )

        QuickSlider(
            label = "Thickness",
            valueLabel = "${String.format("%.1f", config.thicknessDp)} dp",
            value = config.thicknessDp,
            range = crosshairThicknessRange,
            onValueChange = { onConfigChange(config.copy(thicknessDp = it)) },
            onValueChangeFinished = {
                OverlayDiagnostics.log(
                    context,
                    "UI Quick Controls changed thickness=${String.format("%.1f", config.thicknessDp)}"
                )
            }
        )

        QuickSlider(
            label = "Opacity",
            valueLabel = "${(config.opacity * 100).toInt()}%",
            value = config.opacity,
            range = 0.1f..1f,
            onValueChange = { onConfigChange(config.copy(opacity = it)) },
            onValueChangeFinished = {
                OverlayDiagnostics.log(context, "UI Quick Controls changed opacity=${(config.opacity * 100).toInt()}")
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Glow", style = MaterialTheme.typography.labelSmall)
            Switch(
                checked = config.glowEnabled,
                onCheckedChange = {
                    OverlayDiagnostics.log(context, "UI Quick Controls changed glow=$it")
                    onConfigChange(config.copy(glowEnabled = it))
                }
            )
        }
    }
}

@Composable
private fun QuickSlider(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ApexTextSecondary)
        Text(text = valueLabel, style = MaterialTheme.typography.labelSmall, color = ApexTextSecondary)
    }
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = range,
        onValueChangeFinished = onValueChangeFinished
    )
}
