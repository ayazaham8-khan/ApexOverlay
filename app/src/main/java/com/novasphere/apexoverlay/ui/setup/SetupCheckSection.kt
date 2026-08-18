package com.novasphere.apexoverlay.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.novasphere.apexoverlay.overlay.SetupCheckItem
import com.novasphere.apexoverlay.overlay.SetupCheckStatus
import com.novasphere.apexoverlay.ui.theme.ApexAccent
import com.novasphere.apexoverlay.ui.theme.ApexBorder
import com.novasphere.apexoverlay.ui.theme.ApexSurfaceElevated
import com.novasphere.apexoverlay.ui.theme.ApexTextSecondary

@Composable
fun SetupCheckSection(
    checks: List<SetupCheckItem>,
    showFirstRunBanner: Boolean,
    onDismissFirstRunBanner: () -> Unit,
    onOpenOemInstructions: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(text = "Setup Check", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Only \"Display over other apps\" is required. The rest improve reliability during long play sessions.",
            style = MaterialTheme.typography.labelSmall,
            color = ApexTextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (showFirstRunBanner) {
            FirstRunBanner(onDismiss = onDismissFirstRunBanner)
            Spacer(modifier = Modifier.height(12.dp))
        }

        checks.forEachIndexed { index, item ->
            SetupCheckRow(
                item = item,
                onFixClick = {
                    if (item.opensInstructionScreen) {
                        onOpenOemInstructions()
                    } else {
                        item.fixIntent?.let { intent ->
                            runCatching { context.startActivity(intent) }
                        }
                    }
                }
            )
            if (index != checks.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FirstRunBanner(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceElevated)
            .border(width = 1.dp, color = ApexAccent, shape = RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Complete setup before starting your first game",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "A few quick checks now prevent the overlay from disappearing mid-match.",
            style = MaterialTheme.typography.bodyMedium,
            color = ApexTextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onDismiss) {
            Text(text = "Got it", color = ApexAccent)
        }
    }
}

@Composable
private fun SetupCheckRow(item: SetupCheckItem, onFixClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceElevated)
            .border(width = 1.dp, color = ApexBorder, shape = RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = statusSymbol(item.status),
                style = MaterialTheme.typography.titleMedium,
                color = statusColor(item.status)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = item.title, style = MaterialTheme.typography.bodyMedium)
            if (!item.isRequired) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "optional",
                    style = MaterialTheme.typography.labelSmall,
                    color = ApexTextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.description,
            style = MaterialTheme.typography.labelSmall,
            color = ApexTextSecondary,
            modifier = Modifier.fillMaxWidth()
        )
        if (item.fixIntent != null || item.opensInstructionScreen) {
            TextButton(
                onClick = onFixClick,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(text = item.fixLabel ?: "Fix", color = ApexAccent)
            }
        }
    }
}

private fun statusSymbol(status: SetupCheckStatus): String = when (status) {
    SetupCheckStatus.READY -> "✓"
    SetupCheckStatus.ACTION_REQUIRED -> "⚠"
    SetupCheckStatus.MANUAL_CHECK_REQUIRED -> "⚠"
}

private fun statusColor(status: SetupCheckStatus): Color = when (status) {
    SetupCheckStatus.READY -> Color(0xFF39FF88)
    SetupCheckStatus.ACTION_REQUIRED -> Color(0xFFFFC93C)
    SetupCheckStatus.MANUAL_CHECK_REQUIRED -> Color(0xFFFFC93C)
}
