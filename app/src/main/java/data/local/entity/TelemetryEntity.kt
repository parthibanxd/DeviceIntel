package com.example.deviceintel.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telemetry")

data class TelemetryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val timestamp: Long,

    val batteryLevel: Int,

    val ramUsageMb: Long,

    val temperature: Float,

    val freeStorageGb: Float,

    val cpuUsage: Float
)