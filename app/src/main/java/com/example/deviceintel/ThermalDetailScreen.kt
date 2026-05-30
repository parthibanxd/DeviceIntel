package com.example.deviceintel

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
fun ThermalDetailScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val temp = getLiveBatteryTemperature(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
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
        Text("Thermal Telemetry Analysis", color = Color(0xFFF3F4F6), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161D))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailInfoRow(Icons.Outlined.Thermostat, "Thermal Sensor Node 0", "$temp °C")
                DetailInfoRow(Icons.Outlined.LocalFireDepartment, "Throttling Profile", if(temp > 40f) "Active Mitigation Engaged" else "Nominal Threshold")
                DetailInfoRow(Icons.Outlined.Shield, "System Core Safeguard", "Online")
            }
        }
    }
}