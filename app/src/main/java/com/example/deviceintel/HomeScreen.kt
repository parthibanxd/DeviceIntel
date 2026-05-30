package com.example.deviceintel

import android.content.Context
import android.os.Build
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Locale

// --- Theme Colors ---
private val BgDark = Color(0xFF0A0A0F)
private val SurfaceDark = Color(0xFF16161D)
private val TextLight = Color(0xFFF3F4F6)
private val TextMuted = Color(0xFF8B8B99)

private val AccentPurple = Color(0xFF7C6FF0)
private val AccentGreen = Color(0xFF10B981)
private val AccentAmber = Color(0xFFF59E0B)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentRed = Color(0xFFFF2D55)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val context = LocalContext.current

    val batteryPct = remember(context) { getLiveBatteryPercentage(context) }
    val ramInfo = remember(context) { getLiveRamMetrics(context) }
    val thermalTemp = remember(context) { getLiveBatteryTemperature(context) }
    val storageInfo = remember { getLiveStorageMetrics() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        CenteredAppHeader()
        Spacer(modifier = Modifier.height(24.dp))

        DeviceIdentityCard()
        Spacer(modifier = Modifier.height(16.dp))

        HardwareDetailsGrid(context)
        Spacer(modifier = Modifier.height(32.dp))

        SectionLabel("Live Telemetry", Icons.Outlined.Speed)
        Spacer(modifier = Modifier.height(16.dp))

        TelemetryGrid(
            navController = navController,
            batteryPct = batteryPct,
            ramInfo = ramInfo,
            thermalTemp = thermalTemp,
            storageInfo = storageInfo
        )

        Spacer(modifier = Modifier.height(32.dp))

        SectionLabel("System Diagnostics", Icons.Outlined.AutoAwesome)
        Spacer(modifier = Modifier.height(16.dp))
        InsightsSection(thermalTemp = thermalTemp, ramInfo = ramInfo)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CenteredAppHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Device Intel",
            color = TextLight,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
private fun DeviceIdentityCard() {
    val context = LocalContext.current
    val manufacturer = remember { Build.MANUFACTURER.replaceFirstChar { it.uppercase() } }
    val model = remember { Build.MODEL }
    val androidVersion = remember { "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})" }
    val customUI = remember(context) { getCustomUIVersion() }
    val cpu = remember { getChipsetName() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentPurple.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Smartphone, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = manufacturer, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = model, color = TextLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = BgDark, thickness = 2.dp)
            Spacer(modifier = Modifier.height(20.dp))

            InfoRow(icon = Icons.Outlined.Android, label = "OS Version", value = androidVersion)
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(icon = Icons.Outlined.Layers, label = "Custom UI", value = customUI)
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(icon = Icons.Outlined.Memory, label = "Chipset / SoC", value = cpu)
        }
    }
}

@Composable
private fun HardwareDetailsGrid(context: Context) {
    val architecture = remember { Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown" }
    val securityPatch = remember { Build.VERSION.SECURITY_PATCH }
    val cores = remember { Runtime.getRuntime().availableProcessors().toString() }
    val batteryCapacity = remember(context) { "${getBatteryCapacityMah(context)} mAh" }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HardwareMiniCard(modifier = Modifier.weight(1f), title = "Architecture", value = architecture, icon = Icons.Outlined.AccountTree)
            HardwareMiniCard(modifier = Modifier.weight(1f), title = "Security Patch", value = securityPatch, icon = Icons.Outlined.SystemUpdateAlt)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HardwareMiniCard(modifier = Modifier.weight(1f), title = "CPU Cores", value = cores, icon = Icons.Outlined.Speed)
            HardwareMiniCard(modifier = Modifier.weight(1f), title = "Battery Capacity", value = batteryCapacity, icon = Icons.Outlined.ElectricBolt)
        }
    }
}

@Composable
private fun HardwareMiniCard(modifier: Modifier, title: String, value: String, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = value, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionLabel(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TelemetryGrid(
    navController: NavController?,
    batteryPct: Float,
    ramInfo: RamMetrics,
    thermalTemp: Float,
    storageInfo: StorageMetrics
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Battery",
                value = "${(batteryPct * 100).toInt()}%",
                subtitle = "Active usage",
                icon = Icons.Outlined.BatteryChargingFull,
                accentColor = AccentPurple,
                progress = batteryPct,
                onClick = { navController?.navigate("battery_detail") }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "RAM",
                value = "${ramInfo.usedGb} GB",
                subtitle = "Total ${ramInfo.totalGb} GB",
                icon = Icons.Outlined.Memory,
                accentColor = AccentGreen,
                progress = ramInfo.percentage,
                onClick = { navController?.navigate("ram_detail") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Thermal",
                value = "${thermalTemp}°C",
                subtitle = if (thermalTemp > 40f) "Elevated" else "Normal range",
                icon = Icons.Outlined.Thermostat,
                accentColor = if (thermalTemp > 40f) AccentRed else AccentAmber,
                progress = (thermalTemp / 60f).coerceIn(0f, 1f),
                onClick = { navController?.navigate("thermal_detail") }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Storage",
                value = "${storageInfo.usedGb} GB",
                subtitle = "Total ${storageInfo.totalGb} GB",
                icon = Icons.Outlined.Storage,
                accentColor = AccentBlue,
                progress = storageInfo.percentage,
                onClick = { navController?.navigate("storage_detail") }
            )
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    progress: Float,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = TextLight, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress.takeIf { !it.isNaN() } ?: 0f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = accentColor,
                trackColor = BgDark,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = subtitle, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Normal)
        }
    }
}

@Composable
private fun InsightsSection(thermalTemp: Float, ramInfo: RamMetrics) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (ramInfo.percentage > 0.85f) {
            InsightStrip(
                icon = Icons.Outlined.Memory,
                accentColor = AccentAmber,
                title = "High Memory Pressure",
                description = "System is actively paging memory. Close background apps to improve performance."
            )
        } else {
            InsightStrip(
                icon = Icons.Outlined.CheckCircleOutline,
                accentColor = AccentGreen,
                title = "Memory State Optimal",
                description = String.format(Locale.getDefault(), "RAM allocation is stable. %.1f GB remaining for foreground tasks.", ramInfo.totalGb - ramInfo.usedGb)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (thermalTemp > 40f) {
            InsightStrip(
                icon = Icons.Outlined.LocalFireDepartment,
                accentColor = AccentRed,
                title = "Thermal Throttling Risk",
                description = "Device core temperature is elevated ($thermalTemp°C). CPU performance may be temporarily restricted."
            )
        } else {
            InsightStrip(
                icon = Icons.Outlined.Thermostat,
                accentColor = AccentBlue,
                title = "Thermals Normalized",
                description = "Device resources are operating within acceptable thermal thresholds."
            )
        }
    }
}

@Composable
fun InsightStrip(icon: ImageVector, accentColor: Color, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(40.dp).background(accentColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, color = TextLight, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, color = TextMuted, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)
            }
        }
    }
}