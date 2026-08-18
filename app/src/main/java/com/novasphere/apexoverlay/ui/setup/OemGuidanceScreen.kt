package com.novasphere.apexoverlay.ui.setup

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.novasphere.apexoverlay.overlay.OemGuidance
import com.novasphere.apexoverlay.ui.theme.ApexAccent
import com.novasphere.apexoverlay.ui.theme.ApexBorder
import com.novasphere.apexoverlay.ui.theme.ApexSurfaceElevated
import com.novasphere.apexoverlay.ui.theme.ApexTextSecondary

@Composable
fun OemGuidanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val guidance = OemGuidance.forCurrentDevice()

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
                    text = "${guidance.manufacturerLabel} Setup",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                Text(
                    text = guidance.shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ApexTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ApexOverlay cannot open or change these settings for you - Android does not allow apps to control other manufacturers' settings screens. Follow the steps below manually.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ApexTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(guidance.steps) { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ApexSurfaceElevated)
                        .border(width = 1.dp, color = ApexBorder, shape = RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ApexAccent
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = step, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                        runCatching { context.startActivity(intent) }
                    }
                ) {
                    Text(text = "Open App Info", color = ApexAccent)
                }
            }
        }
    }
}
