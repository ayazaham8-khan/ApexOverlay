package com.novasphere.apexoverlay.ui.crosshair

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.novasphere.apexoverlay.overlay.CrosshairOverlayService
import com.novasphere.apexoverlay.overlay.OverlayConfigHolder
import com.novasphere.apexoverlay.overlay.OverlayPermission
import com.novasphere.apexoverlay.ui.theme.ApexAccent
import com.novasphere.apexoverlay.ui.theme.ApexBorder
import com.novasphere.apexoverlay.ui.theme.ApexSurfaceElevated
import com.novasphere.apexoverlay.ui.theme.ApexTextSecondary

@Composable
fun CrosshairScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var config by remember { mutableStateOf(CrosshairConfig()) }
    var selectedPresetId by remember { mutableStateOf(crosshairPresets.first().id) }
    var hasOverlayPermission by remember {
        mutableStateOf(OverlayPermission.hasOverlayPermission(context))
    }

    // Re-check overlay permission whenever this screen's Activity resumes -
    // covers returning from the system "Display over other apps" settings screen.
    DisposableEffect(activity) {
        if (activity == null) {
            return@DisposableEffect onDispose { }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = OverlayPermission.hasOverlayPermission(context)
            }
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    // Keep a running overlay in sync with live UI changes.
    LaunchedEffect(config) {
        OverlayConfigHolder.crosshairConfig = config
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasOverlayPermission = OverlayPermission.hasOverlayPermission(context)
    }

    fun setOverlayEnabled(enabled: Boolean) {
        if (enabled) {
            if (!hasOverlayPermission) {
                permissionLauncher.launch(OverlayPermission.overlayPermissionIntent(context))
                return
            }
            OverlayConfigHolder.crosshairConfig = config
            ContextCompat.startForegroundService(
                context,
                Intent(context, CrosshairOverlayService::class.java)
            )
            config = config.copy(overlayEnabled = true)
        } else {
            context.stopService(Intent(context, CrosshairOverlayService::class.java))
            config = config.copy(overlayEnabled = false)
        }
    }

    Scaffold(
        topBar = { CrosshairTopBar(onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { PreviewSection(config = config) }

            item {
                Column {
                    if (!hasOverlayPermission) {
                        PermissionNotice(
                            onGrantClick = {
                                permissionLauncher.launch(
                                    OverlayPermission.overlayPermissionIntent(context)
                                )
                            }
                        )
                    }
                    ToggleRow(
                        label = "Enable Crosshair",
                        description = if (hasOverlayPermission)
                            "Shows the crosshair above other apps"
                        else
                            "Overlay permission required",
                        checked = config.overlayEnabled,
                        onCheckedChange = { setOverlayEnabled(it) }
                    )
                }
            }

            item {
                PresetSection(
                    selectedId = selectedPresetId,
                    baseConfig = config,
                    onSelect = { preset ->
                        selectedPresetId = preset.id
                        config = config.copy(shape = preset.shape)
                    }
                )
            }

            item {
                ColorSection(
                    selectedColor = config.color,
                    onColorSelected = { config = config.copy(color = it) }
                )
            }

            item {
                ControlSlider(
                    label = "Size",
                    valueLabel = "${config.sizeDp.toInt()} dp",
                    value = config.sizeDp,
                    range = crosshairSizeRange,
                    onValueChange = { config = config.copy(sizeDp = it) }
                )
            }

            item {
                ControlSlider(
                    label = "Thickness",
                    valueLabel = "${String.format("%.1f", config.thicknessDp)} dp",
                    value = config.thicknessDp,
                    range = crosshairThicknessRange,
                    onValueChange = { config = config.copy(thicknessDp = it) }
                )
            }

            item {
                ControlSlider(
                    label = "Opacity",
                    valueLabel = "${(config.opacity * 100).toInt()}%",
                    value = config.opacity,
                    range = 0.1f..1f,
                    onValueChange = { config = config.copy(opacity = it) }
                )
            }

            item {
                ToggleRow(
                    label = "Glow Effect",
                    description = "Adds a soft glow around the crosshair",
                    checked = config.glowEnabled,
                    onCheckedChange = { config = config.copy(glowEnabled = it) }
                )
            }
        }
    }
}

@Composable
private fun CrosshairTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Text(text = "← Back", color = ApexAccent)
        }
        Text(
            text = "Crosshair",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun PermissionNotice(onGrantClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceElevated)
            .border(width = 1.dp, color = ApexAccent, shape = RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(text = "Overlay permission needed", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ApexOverlay needs \"Display over other apps\" permission to show the crosshair above your game.",
            style = MaterialTheme.typography.bodyMedium,
            color = ApexTextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onGrantClick) {
            Text(text = "Grant Permission", color = ApexAccent)
        }
    }
}

@Composable
private fun PreviewSection(config: CrosshairConfig) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(text = "Preview", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(ApexSurfaceElevated)
                .border(width = 1.dp, color = ApexBorder, shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            CrosshairCanvas(config = config, modifier = Modifier.size(140.dp))
        }
    }
}

@Composable
private fun PresetSection(
    selectedId: String,
    baseConfig: CrosshairConfig,
    onSelect: (CrosshairPreset) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Presets",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(crosshairPresets) { preset ->
                val isSelected = preset.id == selectedId
                Column(
                    modifier = Modifier
                        .width(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ApexSurfaceElevated)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) ApexAccent else ApexBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelect(preset) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CrosshairCanvas(
                        config = baseConfig.copy(shape = preset.shape),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = ApexTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSection(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(text = "Color", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            crosshairColorOptions.forEach { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else ApexBorder,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ControlSlider(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = valueLabel, style = MaterialTheme.typography.bodyMedium, color = ApexTextSecondary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = ApexTextSecondary)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
