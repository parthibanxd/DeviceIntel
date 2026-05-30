package com.example.deviceintel.data

import android.content.Context
import com.example.deviceintel.data.local.database.TelemetryDatabase
import com.example.deviceintel.data.local.entity.TelemetryEntity
import kotlinx.coroutines.flow.Flow

class TelemetryRepository(
    context: Context
) {

    private val dao =
        TelemetryDatabase
            .getDatabase(context)
            .telemetryDao()

    suspend fun saveTelemetry(
        telemetry: TelemetryEntity
    ) {

        dao.insertTelemetry(
            telemetry
        )
    }

    fun getTelemetry():
            Flow<List<TelemetryEntity>> {

        return dao.getAllTelemetry()
    }
}