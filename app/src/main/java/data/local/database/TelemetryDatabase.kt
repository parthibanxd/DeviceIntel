package com.example.deviceintel.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.deviceintel.data.local.dao.TelemetryDao
import com.example.deviceintel.data.local.entity.TelemetryEntity

@Database(
    entities = [TelemetryEntity::class],
    version = 2,
    exportSchema = false
)

abstract class TelemetryDatabase :
    RoomDatabase() {

    abstract fun telemetryDao():
            TelemetryDao

    companion object {

        @Volatile
        private var INSTANCE:
                TelemetryDatabase? = null

        fun getDatabase(
            context: Context
        ): TelemetryDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,

                        TelemetryDatabase::class.java,

                        "telemetry_database"
                    )

                        .fallbackToDestructiveMigration()

                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}