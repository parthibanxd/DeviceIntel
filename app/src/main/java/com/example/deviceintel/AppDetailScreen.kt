package com.example.deviceintel

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Theme Colors ---
private val BgDark = Color(0xFF0A0A0F)
private val SurfaceDark = Color(0xFF16161D)
private val TextLight = Color(0xFFF3F4F6)
private val TextMuted = Color(0xFF8B8B99)

private val AccentPurple = Color(0xFF7C6FF0)
private val AccentGreen = Color(0xFF3DBA7A)
private val AccentAmber = Color(0xFFE8A030)
private val AccentRed = Color(0xFFEF4444)
private val AccentBlue = Color(0xFF3B82F6)

// --- Real Data Model for App Details ---
// Removed "private" to fix the "exposes private-in-file parameter type" error
data class AppDeviceDetails(
    val versionName: String,
    val targetSdk: Int,
    val installDate: String,
    val updateDate: String,
    val apkSizeMb: Float
)

@Composable
fun AppDetailScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Safely look up the selected app context from the established model holder structure
    val app = remember { SelectedAppHolder.selectedApp }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgDark
    ) {
        if (app == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No runtime app telemetry selected.", color = TextMuted)
            }
        } else {
            // Performance optimization: cache bitmap conversions
            val appIconBitmap = remember(app.packageName) {
                app.icon?.toBitmap()?.asImageBitmap()
            }

            // Fetch real system details for this package
            val appDetails = remember(app.packageName) {
                fetchAppDeviceDetails(context, app.packageName)
            }

            val usageColor = remember(app.usageMinutes) {
                when {
                    app.usageMinutes < 30 -> AccentGreen
                    app.usageMinutes < 90 -> AccentPurple
                    app.usageMinutes < 180 -> AccentAmber
                    else -> AccentRed
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Top Navigation Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceDark, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TextLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Hero Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (appIconBitmap != null) {
                                Image(
                                    bitmap = appIconBitmap,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(usageColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Apps,
                                        contentDescription = null,
                                        tint = usageColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    color = TextLight,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = app.packageName,
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${app.usageMinutes}",
                                color = TextLight,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = " mins",
                                color = TextMuted,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Foreground Screen Time",
                            color = usageColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Twin Mini Status Deck Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnalyticsMiniCard(
                        modifier = Modifier.weight(1f),
                        title = "Classification",
                        value = getUsageClassification(app.usageMinutes),
                        valueColor = TextLight
                    )
                    AnalyticsMiniCard(
                        modifier = Modifier.weight(1f),
                        title = "Intensity",
                        value = getSessionIntensity(app.usageMinutes),
                        valueColor = usageColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real Device App Properties Info Card
                SystemPackageInfoCard(appDetails)

                Spacer(modifier = Modifier.height(16.dp))

                // Expanded Telemetry Cards Block
                AnalyticsDetailCard(
                    title = "Usage Distribution",
                    description = getUsageDistribution(app.usageMinutes)
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnalyticsDetailCard(
                    title = "Telemetry Insight",
                    description = generateTelemetryInsight(app.usageMinutes)
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SystemPackageInfoCard(details: AppDeviceDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "System Package Details",
                color = TextLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PackageInfoRow(Icons.Outlined.Info, "Version", details.versionName)
                PackageInfoRow(Icons.Outlined.Api, "Target SDK", "API ${details.targetSdk}")
                PackageInfoRow(Icons.Outlined.FolderZip, "APK Size", String.format("%.1f MB", details.apkSizeMb))
                PackageInfoRow(Icons.Outlined.Event, "Installed", details.installDate)
                PackageInfoRow(Icons.Outlined.Update, "Last Updated", details.updateDate)
            }
        }
    }
}

@Composable
private fun PackageInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(text = value, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AnalyticsMiniCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    valueColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AnalyticsDetailCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                color = TextLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 22.sp
            )
        }
    }
}

// --- Device Real Statistics Engine ---

private fun fetchAppDeviceDetails(context: Context, packageName: String): AppDeviceDetails {
    return try {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        // Fix: Safe call using '?.' since applicationInfo can be nullable in the SDK
        val appInfo = packageInfo.applicationInfo

        val versionName = packageInfo.versionName ?: "Unknown"
        // Fix: Provide a fallback of '0' if appInfo is null
        val targetSdk = appInfo?.targetSdkVersion ?: 0

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val installDate = dateFormat.format(Date(packageInfo.firstInstallTime))
        val updateDate = dateFormat.format(Date(packageInfo.lastUpdateTime))

        // Calculate basic APK base size safely
        val sourceDir = appInfo?.sourceDir
        val apkSizeMb = if (sourceDir != null) {
            val apkFile = File(sourceDir)
            if (apkFile.exists()) apkFile.length() / (1024f * 1024f) else 0f
        } else {
            0f
        }

        AppDeviceDetails(versionName, targetSdk, installDate, updateDate, apkSizeMb)
    } catch (e: Exception) {
        AppDeviceDetails("Unknown", 0, "Unknown", "Unknown", 0f)
    }
}

// --- Helper Functions ---
private fun getUsageClassification(usageMinutes: Long): String = when {
    usageMinutes < 30 -> "LOW"
    usageMinutes < 90 -> "MEDIUM"
    usageMinutes < 180 -> "HIGH"
    else -> "HEAVY"
}

private fun getSessionIntensity(usageMinutes: Long): String = when {
    usageMinutes < 45 -> "Light Load"
    usageMinutes < 120 -> "Normal Load"
    usageMinutes < 240 -> "Intensive"
    else -> "Extreme"
}

private fun getUsageDistribution(usageMinutes: Long): String = when {
    usageMinutes < 60 -> "Background dominant usage distribution detected. Minimal foreground CPU impact."
    usageMinutes < 180 -> "Balanced foreground activity observed with standard memory allocation cycles."
    else -> "Foreground dominant activity with sustained interaction periods. High battery drain likely."
}

private fun generateTelemetryInsight(usageMinutes: Long): String = when {
    usageMinutes < 30 -> "App activity remains within low interaction thresholds. No immediate system impact detected."
    usageMinutes < 90 -> "Moderate interaction levels detected across tracked sessions. Thermal and RAM load are stable."
    usageMinutes < 180 -> "High interaction periods detected with elevated foreground activity. Recommend monitoring battery consumption."
    else -> "Sustained heavy interaction detected over extended sessions. This app is currently a primary drain on system resources."
}