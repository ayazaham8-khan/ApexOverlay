package com.novasphere.apexoverlay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.novasphere.apexoverlay.ui.feature.FeaturePlaceholderScreen
import com.novasphere.apexoverlay.ui.home.HomeScreen
import com.novasphere.apexoverlay.ui.navigation.Screen
import com.novasphere.apexoverlay.ui.theme.ApexOverlayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApexOverlayTheme {
                ApexOverlayApp()
            }
        }
    }
}

@Composable
private fun ApexOverlayApp() {
    var currentRoute by rememberSaveable { mutableStateOf(Screen.Home.route) }

    when (currentRoute) {
        Screen.Home.route -> HomeScreen(
            onNavigate = { screen -> currentRoute = screen.route }
        )
        else -> {
            val screen = allScreens.first { it.route == currentRoute }
            FeaturePlaceholderScreen(
                title = screen.title,
                onBack = { currentRoute = Screen.Home.route }
            )
        }
    }
}

private val allScreens = listOf(
    Screen.Home,
    Screen.Crosshair,
    Screen.TouchVisualizer,
    Screen.Grid,
    Screen.FpsPing,
    Screen.Presets,
    Screen.PointsStore,
    Screen.Settings
)
