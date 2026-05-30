package com.example.deviceintel

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BatteryDetailScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Assuming these fetchers are available in your package from HomeScreen.kt
    val batteryPct = (getLiveBatteryPercentage(context) * 100).toInt()
    val temp = getLiveBatteryTemperature(context)
    val capacity = getBatteryCapacityMah(context)

    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    val health = when(intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Excellent"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded / Dead"
        else -> "Good Condition"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F)) // BgDark
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(Color(0xFF16161D), RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color(0xFFF3F4F6))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Battery Engineering Report", color = Color(0xFFF3F4F6), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161D)) // SurfaceDark
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailInfoRow(Icons.Outlined.BatteryFull, "Current Charge", "$batteryPct%")
                DetailInfoRow(Icons.Outlined.ElectricBolt, "Physical Capacity", "$capacity mAh")
                DetailInfoRow(Icons.Outlined.Thermostat, "Core Temperature", "$temp °C")
                DetailInfoRow(Icons.Outlined.Power, "Power State", if(isCharging) "Charging" else "Discharging")
                DetailInfoRow(Icons.Outlined.HealthAndSafety, "Hardware Integrity", health)
            }
        }
    }
}