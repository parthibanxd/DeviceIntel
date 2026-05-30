package com.example.deviceintel

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color

object SelectedAppHolder {
    var selectedApp: AppTelemetryData? = null
}

// Unified data model that both AppsScreen and AppDetailScreen will use
data class AppTelemetryData(
    val appName: String,
    val packageName: String,
    val durationStr: String,
    val usageMinutes: Long,
    val sessions: Int,
    val percentage: Float,
    val color: Color,
    val icon: Drawable?
)