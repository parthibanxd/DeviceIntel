package com.example.deviceintel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.deviceintel.data.local.entity.TelemetryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {

    @Insert
    suspend fun insertTelemetry(
        telemetry: TelemetryEntity
    )

    @Query(
        "SELECT * FROM telemetry ORDER BY timestamp DESC"
    )
    fun getAllTelemetry():
            Flow<List<TelemetryEntity>>
}