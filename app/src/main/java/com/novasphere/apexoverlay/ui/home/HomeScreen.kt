package com.novasphere.apexoverlay.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novasphere.apexoverlay.ui.components.FeatureTile
import com.novasphere.apexoverlay.ui.navigation.Screen
import com.novasphere.apexoverlay.ui.theme.ApexTextSecondary

private data class HomeFeature(
    val screen: Screen,
    val subtitle: String
)

private val homeFeatures = listOf(
    HomeFeature(Screen.Crosshair, "Custom crosshair overlay"),
    HomeFeature(Screen.TouchVisualizer, "Preview your own taps"),
    HomeFeature(Screen.Grid, "Aim-training grid & screen ruler"),
    HomeFeature(Screen.FpsPing, "Refresh rate & network latency"),
    HomeFeature(Screen.Presets, "Saved overlay configurations"),
    HomeFeature(Screen.PointsStore, "Unlock more crosshairs & effects"),
    HomeFeature(Screen.Settings, "App preferences")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Scaffold { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 20.dp
                )
            ) {
                Text(
                    text = "ApexOverlay",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Crosshair, FPS & Gaming Toolkit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ApexTextSecondary
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 20.dp,
                    vertical = 4.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(homeFeatures) { feature ->

                    FeatureTile(
                        title = feature.screen.title,
                        subtitle = feature.subtitle,
                        onClick = {
                            onNavigate(feature.screen)
                        }
                    )
                }
            }

            // Reserved space for the future banner ad.
            // No ad SDK is connected yet.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }
    }
}
