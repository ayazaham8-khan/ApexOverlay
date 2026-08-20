package com.novasphere.apexoverlay.ui.crosshair

import android.content.Intent
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.SideEffect
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
import com.novasphere.apexoverlay.overlay.OverlayDiagnostics
import com.novasphere.apexoverlay.overlay.OverlayPermission
import com.novasphere.apexoverlay.overlay.SetupCheckEvaluator
import com.novasphere.apexoverlay.overlay.SetupCheckStatus
import com.novasphere.apexoverlay.overlay.SetupPreferences
import com.novasphere.apexoverlay.ui.setup.OemGuidanceScreen
import com.novasphere.apexoverlay.ui.setup.SetupCheckSection
import com.novasphere.apexoverlay.ui.theme.ApexAccent
import com.novasphere.apexoverlay.ui.theme.ApexBorder
import com.novasphere.apexoverlay.ui.theme.ApexSurfaceElevated
import com.novasphere.apexoverlay.ui.theme.ApexTextSecondary

private enum class CrosshairSubScreen { MAIN, DEBUG_LOG, OEM_GUIDANCE }

@Composable
fun CrosshairScreen(onBack: () -> Unit) {
    var activeScreen by remember { mutableStateOf(CrosshairSubScreen.MAIN) }

    when (activeScreen) {
        CrosshairSubScreen.MAIN -> CrosshairMainContent(
            onBack = onBack,
            onShowDebugLog = { activeScreen = CrosshairSubScreen.DEBUG_LOG },
            onShowOemGuidance = { activeScreen = CrosshairSubScreen.OEM_GUIDANCE }
        )
        CrosshairSubScreen.DEBUG_LOG -> DebugLogScreen(
            onBack = { activeScreen = CrosshairSubScreen.MAIN }
        )
        CrosshairSubScreen.OEM_GUIDANCE -> OemGuidanceScreen(
            onBack = { activeScreen = CrosshairSubScreen.MAIN }
        )
    }
}

@Composable
private fun CrosshairMainContent(
    onBack: () -> Unit,
    onShowDebugLog: () -> Unit,
    onShowOemGuidance: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var config by remember { mutableStateOf(OverlayConfigHolder.crosshairConfig) }
    var selectedPresetId by remember { mutableStateOf(crosshairPresets.first().id) }
    var setupChecks by remember { mutableStateOf(SetupCheckEvaluator.evaluate(context)) }
    var showFirstRunBanner by remember {
        mutableStateOf(!SetupPreferences.hasSeenSetupIntro(context))
    }

    val hasOverlayPermission = setupChecks
        .first { it.id == "overlay_permission" }
        .status == SetupCheckStatus.READY

    DisposableEffect(activity) {
        if (activity == null) {
            return@DisposableEffect onDispose { }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                setupChecks = SetupCheckEvaluator.evaluate(context)
            }
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    SideEffect {
        OverlayConfigHolder.crosshairConfig = config
    }

    fun setOverlayEnabled(enabled: Boolean) {
        if (enabled) {
            if (!hasOverlayPermission) {
                context.startActivity(OverlayPermission.overlayPermissionIntent(context))
                return
            }
            OverlayConfigHolder.crosshairConfig = config
            OverlayDiagnostics.log(context, "UI starting CrosshairOverlayService")
            ContextCompat.startForegroundService(
                context,
                Intent(context, CrosshairOverlayService::class.java)
            )
            config = config.copy(overlayEnabled = true)
        } else {
            OverlayDiagnostics.log(context, "UI stopping CrosshairOverlayService")
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
                SetupCheckSection(
                    checks = setupChecks,
                    showFirstRunBanner = showFirstRunBanner,
                    onDismissFirstRunBanner = {
                        SetupPreferences.markSetupIntroSeen(context)
                        showFirstRunBanner = false
                    },
                    onOpenOemInstructions = onShowOemGuidance
                )
            }

            item {
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

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onShowDebugLog) {
                        Text(
                            text = "View Debug Log",
                            color = ApexTextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
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
private fun DebugLogScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var logText by remember { mutableStateOf(OverlayDiagnostics.readLog(context)) }

    Scaffold(
        topBar = {
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
                    text = "Debug Log",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { logText = OverlayDiagnostics.readLog(context) }) {
                    Text(text = "Refresh", color = ApexAccent)
                }
                TextButton(onClick = {
                    OverlayDiagnostics.clearLog(context)
                    logText = OverlayDiagnostics.readLog(context)
                }) {
                    Text(text = "Clear", color = ApexTextSecondary)
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                item {
                    Text(
                        text = logText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ApexTextSecondary
                    )
                }
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
