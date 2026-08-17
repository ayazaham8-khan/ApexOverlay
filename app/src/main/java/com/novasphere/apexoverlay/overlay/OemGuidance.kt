package com.novasphere.apexoverlay.overlay

import android.os.Build

data class OemGuidanceContent(
    val manufacturerLabel: String,
    val shortDescription: String,
    val steps: List<String>,
    val isKnownAggressive: Boolean
)

object OemGuidance {

    fun forCurrentDevice(): OemGuidanceContent {
        val manufacturer = Build.MANUFACTURER.lowercase()

        return when {
            manufacturer.contains("infinix") ||
                manufacturer.contains("tecno") ||
                manufacturer.contains("itel") ||
                manufacturer.contains("transsion") -> transsionGuidance(manufacturer)

            manufacturer.contains("xiaomi") ||
                manufacturer.contains("redmi") ||
                manufacturer.contains("poco") -> xiaomiGuidance()

            manufacturer.contains("oppo") ||
                manufacturer.contains("realme") ||
                manufacturer.contains("oneplus") -> colorOsGuidance()

            manufacturer.contains("vivo") -> vivoGuidance()

            manufacturer.contains("samsung") -> samsungGuidance()

            else -> genericGuidance()
        }
    }

    private fun transsionGuidance(manufacturer: String): OemGuidanceContent {
        val label = when {
            manufacturer.contains("tecno") -> "Tecno (HiOS)"
            manufacturer.contains("itel") -> "itel (itelOS)"
            else -> "Infinix (XOS)"
        }
        return OemGuidanceContent(
            manufacturerLabel = label,
            shortDescription = "$label has its own background app manager, separate from Android's battery settings.",
            steps = listOf(
                "Settings → Apps & Notifications → App Management → ApexOverlay → enable Autostart",
                "Settings → Battery → Power Manager (or Battery Lab) → ApexOverlay → Allow Background Running",
                "Settings → Additional Settings → RAM Manager (if present) → add ApexOverlay to exceptions",
                "Open Recents, swipe down on the ApexOverlay card, and tap the lock icon"
            ),
            isKnownAggressive = true
        )
    }

    private fun xiaomiGuidance(): OemGuidanceContent {
        return OemGuidanceContent(
            manufacturerLabel = "Xiaomi (MIUI/HyperOS)",
            shortDescription = "MIUI/HyperOS has its own Autostart and background permission manager.",
            steps = listOf(
                "Settings → Apps → Manage apps → ApexOverlay → Autostart → enable it",
                "Settings → Apps → Permissions → Other permissions → ApexOverlay → allow \"Display pop-up windows while running in background\"",
                "Security app → Battery → App battery saver → ApexOverlay → No restrictions"
            ),
            isKnownAggressive = true
        )
    }

    private fun colorOsGuidance(): OemGuidanceContent {
        return OemGuidanceContent(
            manufacturerLabel = "ColorOS (Oppo/Realme/OnePlus)",
            shortDescription = "ColorOS has its own battery and startup manager.",
            steps = listOf(
                "Settings → Battery → App Battery Management → ApexOverlay → Allow background activity",
                "Settings → Privacy → Startup Manager → enable ApexOverlay",
                "Open Recents, long-press the ApexOverlay card, and lock it"
            ),
            isKnownAggressive = true
        )
    }

    private fun vivoGuidance(): OemGuidanceContent {
        return OemGuidanceContent(
            manufacturerLabel = "Vivo (Funtouch/OriginOS)",
            shortDescription = "Vivo has its own background app permission manager.",
            steps = listOf(
                "Settings → Battery → Background power consumption management → ApexOverlay → Allow",
                "Settings → More settings → Permission management → Autostart → enable ApexOverlay"
            ),
            isKnownAggressive = true
        )
    }

    private fun samsungGuidance(): OemGuidanceContent {
        return OemGuidanceContent(
            manufacturerLabel = "Samsung (One UI)",
            shortDescription = "One UI keeps a separate list of apps it puts to sleep to save battery.",
            steps = listOf(
                "Settings → Apps → ApexOverlay → Battery → set to \"Unrestricted\"",
                "Settings → Battery → Background usage limits → make sure ApexOverlay isn't in the \"Sleeping apps\" or \"Deep sleeping apps\" list"
            ),
            isKnownAggressive = false
        )
    }

    private fun genericGuidance(): OemGuidanceContent {
        return OemGuidanceContent(
            manufacturerLabel = "Device",
            shortDescription = "Some manufacturers restrict background apps beyond standard Android battery settings.",
            steps = listOf(
                "Check your phone's Settings app for a Battery or Power Manager section",
                "Look for an app-specific \"Autostart\", \"Background activity\", or \"Protected apps\" toggle and enable it for ApexOverlay"
            ),
            isKnownAggressive = false
        )
    }
}
