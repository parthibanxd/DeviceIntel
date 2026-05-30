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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StorageDetailScreen(onBack: () -> Unit) {
    val storage = getLiveStorageMetrics()

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
        Text("Storage Hierarchy Map", color = Color(0xFFF3F4F6), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161D))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailInfoRow(Icons.Outlined.Storage, "Total Memory Bus Space", "${storage.totalGb} GB")
                DetailInfoRow(Icons.Outlined.FolderCopy, "Used Memory Space", "${storage.usedGb} GB")
                DetailInfoRow(Icons.Outlined.FolderZip, "Free System Sectors", "${storage.totalGb - storage.usedGb} GB")
                DetailInfoRow(Icons.Outlined.AlignHorizontalLeft, "Volume Occupancy", "${(storage.percentage * 100).toInt()}%")
            }
        }
    }
}
