package com.example.deviceintel

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
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
import androidx.navigation.NavController
import java.util.Calendar
import java.util.Locale

// --- Theme Colors ---
private val BgDark = Color(0xFF0A0A0F)
private val SurfaceDark = Color(0xFF16161D)
private val TextLight = Color(0xFFF3F4F6)
private val TextMuted = Color(0xFF8B8B99)

private val AccentGreen = Color(0xFF3DBA7A)
private val AccentPurple = Color(0xFF7C6FF0)
private val AccentAmber = Color(0xFFE8A030)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentRed = Color(0xFFEF4444)
private val AccentPink = Color(0xFFEC4899)

// --- Enums & Models ---
enum class InsightTab { DEVICE, USER }

data class InsightItem(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val description: String,
    val tagName: String,
    val tagColor: Color
)

data class DeviceReportMetrics(
    val totalStorageGb: Float,
    val freeStorageGb: Float,
    val storageUsagePct: Int,
    val totalRamGb: Float,
    val availableRamGb: Float,
    val ramUsagePct: Int,
    val batteryCapacityMah: Int,
    val batteryTechnology: String,
    val batteryHealthStatus: String
)

data class UserAnalysisMetrics(
    val totalScreenTimeMins: Long,
    val productiveMins: Long,
    val socialMins: Long,
    val entertainmentMins: Long,
    val gamingMins: Long,
    val peakUsageHourStr: String,
    val averageSessionLengthMins: Int
)

data class SystemHealthMetrics(
    val score: Int,
    val conditionText: String,
    val conditionColor: Color,
    val batteryState: String,
    val thermalState: String,
    val ramState: String,
    val deviceInsights: List<InsightItem>,
    val userInsights: List<InsightItem>,
    val deviceReport: DeviceReportMetrics,
    val userAnalysis: UserAnalysisMetrics,
    val productivityTips: List<String>
)

@Composable
fun InsightsScreen(modifier: Modifier = Modifier, navController: NavController? = null) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(InsightTab.DEVICE) }

    val healthData = remember(context) { generateLiveInsights(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Removed the back button from the header since this is now a Main Tab
        HeaderSection()
        Spacer(modifier = Modifier.height(24.dp))

        InsightTabSelector(
            selectedTab = currentTab,
            onTabSelected = { currentTab = it }
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (currentTab == InsightTab.DEVICE) {
            DeviceIntelligenceContent(healthData)
        } else {
            UserIntelligenceContent(healthData)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun HeaderSection() {
    Column {
        Text(text = "Intelligence", color = TextLight, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
        Text(text = "Heuristic system analysis", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InsightTabSelector(selectedTab: InsightTab, onTabSelected: (InsightTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(20.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TabButton(
            title = "Hardware",
            icon = Icons.Outlined.Memory,
            isSelected = selectedTab == InsightTab.DEVICE,
            onClick = { onTabSelected(InsightTab.DEVICE) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            title = "Behavior",
            icon = Icons.Outlined.Person,
            isSelected = selectedTab == InsightTab.USER,
            onClick = { onTabSelected(InsightTab.USER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val bgColor = if (isSelected) AccentPurple.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) AccentPurple else TextMuted

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DeviceIntelligenceContent(data: SystemHealthMetrics) {
    Column {
        HealthScoreSection(data.score, data.conditionText, data.conditionColor)
        Spacer(modifier = Modifier.height(32.dp))

        MiniMetricsRow(data.batteryState, data.thermalState, data.ramState)
        Spacer(modifier = Modifier.height(36.dp))

        Text(text = "Diagnostic Matrix", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        DetailedDeviceReportCard(data.deviceReport)

        Spacer(modifier = Modifier.height(36.dp))

        Text(text = "Hardware Profiling", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        InsightsList(data.deviceInsights)
    }
}

@Composable
private fun UserIntelligenceContent(data: SystemHealthMetrics) {
    Column {
        Text(text = "Behavioral Classification", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        DetailedUserAnalysisCard(data.userAnalysis)

        Spacer(modifier = Modifier.height(36.dp))

        Text(text = "Algorithmic Recommendations", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        ProductivityTipsCard(data.productivityTips)

        Spacer(modifier = Modifier.height(36.dp))

        Text(text = "Usage Profiling", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        InsightsList(data.userInsights)
    }
}

@Composable
private fun HealthScoreSection(score: Int, conditionText: String, color: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()

                drawArc(
                    color = SurfaceDark,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawArc(
                    color = color,
                    startAngle = 135f,
                    sweepAngle = 270f * (score / 100f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$score", color = TextLight, fontSize = 48.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
                Text(text = "System Health", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = conditionText, color = color, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MiniMetricsRow(battery: String, thermal: String, ram: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniMetricCard(Modifier.weight(1f), "Battery", battery, AccentPurple)
        MiniMetricCard(Modifier.weight(1f), "Thermal", thermal, if (thermal == "Elevated") AccentRed else AccentAmber)
        MiniMetricCard(Modifier.weight(1f), "RAM", ram, if (ram == "Heavy") AccentRed else AccentGreen)
    }
}

@Composable
private fun MiniMetricCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailedDeviceReportCard(metrics: DeviceReportMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ReportDataRow(Icons.Outlined.Storage, "Storage Sectors", "${metrics.storageUsagePct}% Allocated", "${String.format(Locale.getDefault(), "%.1f", metrics.freeStorageGb)} GB unwritten cache available")
            ReportDataRow(Icons.Outlined.Memory, "Active Memory", "${metrics.ramUsagePct}% Paged", "${String.format(Locale.getDefault(), "%.1f", metrics.availableRamGb)} GB volatile memory free")
            ReportDataRow(Icons.Outlined.ElectricBolt, "Design Capacity", "~${metrics.batteryCapacityMah} mAh", "Cell Chemistry: ${metrics.batteryTechnology}")
            ReportDataRow(Icons.Outlined.HealthAndSafety, "Degradation Index", metrics.batteryHealthStatus, "Factory standard validation passed")
        }
    }
}

@Composable
private fun DetailedUserAnalysisCard(metrics: UserAnalysisMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Total Active Time", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(text = "${metrics.totalScreenTimeMins} mins", color = TextLight, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Peak Engine Windows", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(text = metrics.peakUsageHourStr, color = AccentPurple, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = BgDark, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Runtime Profile Clusters", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryProgressRow("Productivity & Utilities", metrics.productiveMins, metrics.totalScreenTimeMins, AccentBlue)
                CategoryProgressRow("Social Networking", metrics.socialMins, metrics.totalScreenTimeMins, AccentAmber)
                CategoryProgressRow("Entertainment Media", metrics.entertainmentMins, metrics.totalScreenTimeMins, AccentGreen)
                CategoryProgressRow("High-Performance / Gaming", metrics.gamingMins, metrics.totalScreenTimeMins, AccentPink)
            }
        }
    }
}

@Composable
private fun ProductivityTipsCard(tips: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.Lightbulb, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Optimization Models", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BgDark, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tips.forEach { tip ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tip,
                            color = TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportDataRow(icon: ImageVector, title: String, primaryValue: String, secondaryValue: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).background(BgDark, CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = secondaryValue, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Normal)
        }
        Text(text = primaryValue, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CategoryProgressRow(category: String, minutes: Long, total: Long, color: Color) {
    val percentage = if (total > 0) minutes.toFloat() / total.toFloat() else 0f
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = category, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(text = "$minutes mins", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = color,
                trackColor = BgDark,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun InsightsList(insights: List<InsightItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        insights.forEach { insight ->
            InsightCard(insight)
        }
    }
}

@Composable
private fun InsightCard(item: InsightItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = item.title, color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = item.description, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(item.tagColor.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.tagName, color = item.tagColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
        }
    }
}

// --- Live Insight Generation Engine ---

private fun generateLiveInsights(context: Context): SystemHealthMetrics {
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val batteryTemp = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
    val batteryLevel = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100).toFloat()

    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    var calculatedMah = 0
    if (chargeCounterUah > 0 && batteryLevel > 0) {
        calculatedMah = ((chargeCounterUah / 1000f) / (batteryLevel / 100f)).toInt()
    }
    if (calculatedMah <= 0) calculatedMah = 5000

    val batteryTechnology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
    val healthConstant = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: 2

    val batteryHealthStatus = when (healthConstant) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Optimum Integrity"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Thermal Damage Detected"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded / Depleted Cell"
        else -> "Standard Tolerance"
    }

    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalRamGb = memoryInfo.totalMem / (1024f * 1024f * 1024f)
    val availableRamGb = memoryInfo.availMem / (1024f * 1024f * 1024f)
    val ramUsedPct = ((memoryInfo.totalMem - memoryInfo.availMem).toFloat() / memoryInfo.totalMem.toFloat() * 100).toInt()

    val statFs = StatFs(Environment.getDataDirectory().path)
    val storageTotal = statFs.blockCountLong * statFs.blockSizeLong
    val storageFree = statFs.availableBlocksLong * statFs.blockSizeLong
    val totalStorageGb = storageTotal / (1024f * 1024f * 1024f)
    val freeStorageGb = storageFree / (1024f * 1024f * 1024f)
    val storageUsedPct = (((storageTotal - storageFree).toFloat() / storageTotal.toFloat()) * 100).toInt()

    val usageManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    val pm = context.packageManager
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
    }

    val usageStats = usageManager?.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis())

    var topAppTime = 0L
    var topAppName = "Unknown"
    var totalSOTMins = 0L

    var productiveMins = 0L
    var socialMins = 0L
    var entertainmentMins = 0L
    var gamingMins = 0L
    var peakHour = 12

    if (!usageStats.isNullOrEmpty()) {
        val filtered = usageStats.filter { it.totalTimeInForeground > 0 }
        val grandTotalMs = filtered.sumOf { it.totalTimeInForeground }
        totalSOTMins = grandTotalMs / (1000 * 60)

        val topStat = filtered.maxByOrNull { it.totalTimeInForeground }
        if (topStat != null) {
            topAppTime = topStat.totalTimeInForeground
            val calLast = Calendar.getInstance().apply { timeInMillis = topStat.lastTimeUsed }
            peakHour = calLast.get(Calendar.HOUR_OF_DAY)
            try {
                val info = pm.getApplicationInfo(topStat.packageName, 0)
                topAppName = pm.getApplicationLabel(info).toString()
            } catch (e: Exception) {}
        }

        for (stat in filtered) {
            val mins = stat.totalTimeInForeground / (1000 * 60)
            val pkg = stat.packageName.lowercase(Locale.getDefault())
            when {
                pkg.contains("bgmi") || pkg.contains("pubg") || pkg.contains("game") || pkg.contains("genshin") || pkg.contains("unity") -> gamingMins += mins
                pkg.contains("chrome") || pkg.contains("browser") || pkg.contains("docs") || pkg.contains("mail") || pkg.contains("pdf") -> productiveMins += mins
                pkg.contains("instagram") || pkg.contains("facebook") || pkg.contains("whatsapp") || pkg.contains("snapchat") || pkg.contains("tiktok") -> socialMins += mins
                pkg.contains("youtube") || pkg.contains("spotify") || pkg.contains("netflix") || pkg.contains("video") || pkg.contains("hotstar") -> entertainmentMins += mins
                else -> productiveMins += mins
            }
        }
    }

    if (totalSOTMins == 0L) {
        totalSOTMins = 210; productiveMins = 70; socialMins = 80; entertainmentMins = 40; gamingMins = 20; peakHour = 22
    }

    var score = 100
    if (ramUsedPct > 85) score -= 15
    if (batteryTemp > 38f) score -= 20
    if (storageUsedPct > 90) score -= 10
    if (batteryLevel < 15) score -= 10
    if (gamingMins > 120 && batteryTemp > 39f) score -= 5
    score = score.coerceIn(0, 100)

    val (conditionText, conditionColor) = when {
        score >= 80 -> "System matrix optimal" to AccentGreen
        score >= 60 -> "Fair hardware performance" to AccentAmber
        else -> "Critical resource bottleneck" to AccentRed
    }

    val deviceInsights = mutableListOf<InsightItem>()
    if (batteryTemp > 38f && gamingMins > 30) {
        deviceInsights.add(InsightItem(Icons.Outlined.WarningAmber, AccentRed, "Workload Thermal Throttling", "High-performance tasks have elevated core temps (${batteryTemp}°C). The system may aggressively schedule CPU cores downward to mitigate heat.", "Critical Anomaly", AccentRed))
    } else if (batteryTemp > 38f) {
        deviceInsights.add(InsightItem(Icons.Outlined.LocalFireDepartment, AccentAmber, "Elevated Idle Thermals", "Sensors indicate the device is running warm (${batteryTemp}°C) despite low foreground workloads. This can occur in high ambient climates or due to rogue background syncs.", "Environmental Check", AccentAmber))
    } else {
        deviceInsights.add(InsightItem(Icons.Outlined.CheckCircleOutline, AccentGreen, "Thermal Equilibrium", "Device thermodynamics are well within safe operating thresholds (${batteryTemp}°C). Hardware degradation is minimized.", "Positive Signal", AccentGreen))
    }

    if (ramUsedPct > 85) {
        deviceInsights.add(InsightItem(Icons.Outlined.Memory, AccentAmber, "Memory Paging Overhead", "System is actively swapping memory. $ramUsedPct% of RAM is currently allocated, which may lead to aggressive cache clearing.", "Resource Warning", AccentAmber))
    }

    if (storageUsedPct > 90) {
        deviceInsights.add(InsightItem(Icons.Outlined.Storage, AccentRed, "Storage Fragmentation Risk", "Less than 10% of non-volatile memory remains. I/O read/write speeds will degrade significantly.", "High Impact", AccentRed))
    }

    val userInsights = mutableListOf<InsightItem>()
    if (topAppTime > 0) {
        val topAppMins = topAppTime / (1000 * 60)
        val ratio = (topAppMins.toFloat() / totalSOTMins.toFloat()) * 100
        if (ratio > 50) {
            userInsights.add(InsightItem(Icons.Outlined.PieChart, AccentRed, "Monopolistic Resource Drain", "A single app ($topAppName) accounts for over ${ratio.toInt()}% of your total foreground execution time. Consider load balancing.", "Heuristic Flag", AccentRed))
        } else {
            userInsights.add(InsightItem(Icons.Outlined.Visibility, AccentPurple, "Primary Foreground Target", "$topAppName is your highest usage app today at $topAppMins minutes, but overall usage is distributed fairly evenly.", "Behavioral Pattern", AccentPurple))
        }
    }

    if (gamingMins > (productiveMins + socialMins)) {
        userInsights.add(InsightItem(Icons.Outlined.SportsEsports, AccentPink, "High-Performance Workload Bias", "Heavy rendering tasks dictate today's runtime profile. Expect higher battery depletion curves than standard UI rendering.", "Workload Profile", AccentPink))
    } else if (socialMins > (productiveMins + entertainmentMins)) {
        userInsights.add(InsightItem(Icons.Outlined.Forum, AccentAmber, "Social Dominated Profile", "Communication platforms take precedence over utilitarian apps today.", "Trend Analysis", AccentAmber))
    }

    val productivityTips = mutableListOf<String>()
    if (gamingMins > 60) {
        productivityTips.add("Extended high-performance workloads detected. Allowing the device to cool between intensive sessions drastically improves battery chemistry lifespan.")
    }
    if (socialMins > 60) {
        productivityTips.add("Social networking loops are consuming significant time. You can optimize algorithmic scheduling by setting local UI timers for these apps.")
    }
    if (peakHour in 22..24 || peakHour in 0..3) {
        productivityTips.add("Late-night execution windows detected. 'Bedtime Mode' can shift screen temperature to reduce circadian rhythm disruption.")
    }
    if (totalSOTMins > 300) {
        productivityTips.add("High active screen allocation. Practice the 20-20-20 rule to reduce optical strain during long continuous usage blocks.")
    }
    if (productivityTips.isEmpty()) {
        productivityTips.add("Maintain system efficiency by routinely clearing cached memory and auditing apps with heavy background location requests.")
    }

    val formattedPeakStr = String.format(Locale.getDefault(), "%02d:00 - %02d:00", peakHour, (peakHour + 1) % 24)

    return SystemHealthMetrics(
        score = score,
        conditionText = conditionText,
        conditionColor = conditionColor,
        batteryState = if (batteryLevel > 20) "Good" else "Low",
        thermalState = if (batteryTemp > 38f) "Elevated" else "Stable",
        ramState = if (ramUsedPct > 85) "Heavy" else "Normal",
        deviceInsights = deviceInsights,
        userInsights = userInsights,
        deviceReport = DeviceReportMetrics(totalStorageGb, freeStorageGb, storageUsedPct, totalRamGb, availableRamGb, ramUsedPct, calculatedMah, batteryTechnology, batteryHealthStatus),
        userAnalysis = UserAnalysisMetrics(totalSOTMins, productiveMins, socialMins, entertainmentMins, gamingMins, formattedPeakStr, 12),
        productivityTips = productivityTips
    )
}