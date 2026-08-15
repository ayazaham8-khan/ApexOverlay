package com.novasphere.apexoverlay.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novasphere.apexoverlay.ui.components.FeatureTile
import com.novasphere.apexoverlay.ui.navigation.Screen
import com.novasphere.apexoverlay.ui.theme.ApexTextSecondary

private data class HomeFeature(val screen: Screen, val subtitle: String)

private val homeFeatures = listOf(
    HomeFeature(Screen.Crosshair, "Custom crosshair overlay"),
    HomeFeature(Screen.TouchVisualizer, "Preview your own taps"),
    HomeFeature(Screen.Grid, "Aim-training grid & screen ruler"),
    HomeFeature(Screen.FpsPing, "Refresh rate & network latency"),
    HomeFeature(Screen.Presets, "Saved overlay configurations"),
    HomeFeature(Screen.PointsStore, "Unlock more crosshairs & effects"),
    HomeFeature(Screen.Settings, "App preferences")
)

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    Text(text = "ApexOverlay", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "Crosshair, FPS & Gaming Toolkit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ApexTextSecondary
                    )
                }
            }

            items(homeFeatures) { feature ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    FeatureTile(
                        title = feature.screen.title,
                        subtitle = feature.subtitle,
                        onClick = { onNavigate(feature.screen) }
                    )
                }
            }

            item {
                // Reserved space for the future in-app banner ad slot.
                // Empty in this milestone - no ad SDK is wired in yet, and this
                // region is intentionally separate from the overlay engine.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        }
    }
}
