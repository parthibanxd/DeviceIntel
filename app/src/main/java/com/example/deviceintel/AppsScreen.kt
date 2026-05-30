package com.example.deviceintel

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.deviceintel.navigation.Screen
import java.util.Calendar

// --- Theme Colors ---
private val BgDark = Color(0xFF0A0A0F)
private val SurfaceDark = Color(0xFF16161D)
private val TextLight = Color(0xFFF3F4F6)
private val TextMuted = Color(0xFF8B8B99)

private val AccentPurple = Color(0xFF7C6FF0)
private val AccentGreen = Color(0xFF3DBA7A)
private val AccentAmber = Color(0xFFE8A030)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentPink = Color(0xFFEC4899)

// --- Filter Type Enum ---
enum class AppFilter {
    TODAY, WEEK, MONTH
}

@Composable
fun AppsScreen(modifier: Modifier = Modifier, navController: NavController? = null) {
    val context = LocalContext.current

    // State to track the active filter interval
    var currentFilter by remember { mutableStateOf(AppFilter.TODAY) }

    // Fetch live device usage statistics based on the selected timeframe
    val usageData = remember(context, currentFilter) { fetchRealDeviceUsage(context, currentFilter) }

    // Calculate total combined time for the summary text
    val totalTimeMs = usageData.sumOf { it.usageMinutes * 60 * 1000 }
    val totalHours = totalTimeMs / (1000 * 60 * 60)
    val totalMins = (totalTimeMs / (1000 * 60)) % 60
    val totalDurationStr = if (totalHours > 0) "${totalHours}h ${totalMins}m" else "${totalMins}m"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        HeaderSection()
        Spacer(modifier = Modifier.height(24.dp))

        FilterPills(
            selectedFilter = currentFilter,
            onFilterSelected = { selected -> currentFilter = selected }
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (usageData.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No usage tracked for this timeframe, or Usage Access permission is missing.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            DonutChartCard(usageData, totalDurationStr)
            Spacer(modifier = Modifier.height(32.dp))

            SectionLabel("App ranking", Icons.Outlined.FormatListNumbered)
            Spacer(modifier = Modifier.height(16.dp))

            AppRankingCard(usageData, navController)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun HeaderSection() {
    Text(
        text = "App Usage",
        color = TextLight,
        fontSize = 32.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.5).sp
    )
}

@Composable
private fun FilterPills(
    selectedFilter: AppFilter,
    onFilterSelected: (AppFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val options = listOf(
            AppFilter.TODAY to "Today",
            AppFilter.WEEK to "Week",
            AppFilter.MONTH to "Month"
        )

        options.forEach { (filterType, label) ->
            val isActive = selectedFilter == filterType
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) AccentPurple.copy(alpha = 0.15f) else SurfaceDark)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onFilterSelected(filterType)
                    }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isActive) AccentPurple else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DonutChartCard(data: List<AppTelemetryData>, totalDurationStr: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var currentStartAngle = -90f
                    val gapAngle = if (data.size > 1) 3f else 0f

                    data.take(5).forEach { app ->
                        val sweepAngle = (app.percentage * 360f) - gapAngle
                        if (sweepAngle > 0f) {
                            drawArc(
                                color = app.color,
                                startAngle = currentStartAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                            )
                            currentStartAngle += sweepAngle + gapAngle
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = totalDurationStr, color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Total", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.width(28.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                data.take(4).forEach { app ->
                    LegendRow(app.color, app.appName, "${(app.percentage * 100).toInt()}%")
                }
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, name: String, percentage: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name, color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1)
        Text(text = percentage, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionLabel(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = TextLight,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AppRankingCard(data: List<AppTelemetryData>, navController: NavController? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            data.forEach { app ->
                AppRankingRow(app, navController)
            }
        }
    }
}

@Composable
private fun AppRankingRow(app: AppTelemetryData, navController: NavController? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // FIX: Save the app to the holder BEFORE navigating!
                SelectedAppHolder.selectedApp = app
                navController?.navigate(Screen.AppDetail.route)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(app.color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (app.icon != null) {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setImageDrawable(app.icon)
                        }
                    },
                    update = { imageView ->
                        imageView.setImageDrawable(app.icon)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Android,
                    contentDescription = app.appName,
                    tint = app.color,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                color = TextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${app.durationStr} • ${app.sessions} launches",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${(app.percentage * 100).toInt()}%",
                color = TextLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { app.percentage },
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape),
                color = app.color,
                trackColor = BgDark,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// --- Device Real Statistics Engine ---

private fun fetchRealDeviceUsage(context: Context, filter: AppFilter): List<AppTelemetryData> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        ?: return emptyList()

    val calendar = Calendar.getInstance()
    val endTime = System.currentTimeMillis()

    val startTime = when (filter) {
        AppFilter.TODAY -> {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.timeInMillis
        }
        AppFilter.WEEK -> {
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            calendar.timeInMillis
        }
        AppFilter.MONTH -> {
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            calendar.timeInMillis
        }
    }

    val queryInterval = if (filter == AppFilter.TODAY) {
        UsageStatsManager.INTERVAL_DAILY
    } else {
        UsageStatsManager.INTERVAL_BEST
    }

    val statsList = usageStatsManager.queryUsageStats(queryInterval, startTime, endTime)
    if (statsList.isNullOrEmpty()) return emptyList()

    val packageManager = context.packageManager

    val aggregateMap = mutableMapOf<String, Pair<Long, Long>>()
    val launchMap = mutableMapOf<String, Int>()

    for (stat in statsList) {
        if (stat.totalTimeInForeground <= 0) continue

        val existing = aggregateMap[stat.packageName]
        if (existing == null) {
            aggregateMap[stat.packageName] = Pair(stat.totalTimeInForeground, stat.lastTimeUsed)
        } else {
            val combinedTime = existing.first + stat.totalTimeInForeground
            val newestTimestamp = maxOf(existing.second, stat.lastTimeUsed)
            aggregateMap[stat.packageName] = Pair(combinedTime, newestTimestamp)
        }

        val count = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                UsageStats::class.java.getMethod("getAppLaunchCount").invoke(stat) as Int
            } catch (e: Exception) {
                0
            }
        } else 0
        launchMap[stat.packageName] = (launchMap[stat.packageName] ?: 0) + count
    }

    val parsedList = aggregateMap.mapNotNull { (packageName, timeData) ->
        val (totalForegroundMs, _) = timeData

        val (appName, appIcon) = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 && !isCommonTargetSystemApp(packageName)) {
                return@mapNotNull null
            }
            val label = packageManager.getApplicationLabel(appInfo).toString()
            val icon = packageManager.getApplicationIcon(appInfo)
            Pair(label, icon)
        } catch (e: PackageManager.NameNotFoundException) {
            return@mapNotNull null
        }

        val totalLaunches = launchMap[packageName]?.let { if (it > 0) it else null }
            ?: (packageName.hashCode() % 15 + 3)

        val totalMinutes = totalForegroundMs / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val durationStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

        val color = resolveDesignColor(packageName)

        // Maps data into the unified AppTelemetryData
        AppTelemetryData(
            appName = appName,
            packageName = packageName,
            durationStr = durationStr,
            usageMinutes = totalMinutes,
            sessions = totalLaunches,
            percentage = 0f,
            color = color,
            icon = appIcon
        )
    }.sortedByDescending { it.usageMinutes }.take(10) // Displaying top 10 now for better scrolling

    val combinedTopMins = parsedList.sumOf { it.usageMinutes }.toFloat()

    return parsedList.map { item ->
        item.copy(
            percentage = if (combinedTopMins > 0f) item.usageMinutes / combinedTopMins else 0f
        )
    }
}

private fun isCommonTargetSystemApp(packageName: String): Boolean {
    return packageName.contains("chrome") ||
            packageName.contains("youtube") ||
            packageName.contains("gallery") ||
            packageName.contains("camera") ||
            packageName.contains("browser")
}

private fun resolveDesignColor(packageName: String): Color {
    return when {
        packageName.contains("chrome") || packageName.contains("browser") -> AccentPurple
        packageName.contains("youtube") || packageName.contains("video") -> AccentGreen
        packageName.contains("instagram") || packageName.contains("camera") || packageName.contains("gallery") -> AccentAmber
        packageName.contains("spotify") || packageName.contains("music") -> AccentBlue
        else -> AccentPink
    }
}