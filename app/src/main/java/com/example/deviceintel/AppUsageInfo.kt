package com.example.deviceintel

import android.graphics.drawable.Drawable

data class AppUsageInfo(

    val appName: String,

    val packageName: String,

    val usageMinutes: Long,

    val icon: Drawable?
)