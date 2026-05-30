package com.example.deviceintel

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.concurrent.TimeUnit

object ScreenTimeHelper {

    fun getTotalScreenTimeMinutes(
        context: Context
    ): Long {

        val usageStatsManager =

            context.getSystemService(
                Context.USAGE_STATS_SERVICE
            ) as UsageStatsManager

        val endTime =
            System.currentTimeMillis()

        val startTime =
            endTime - TimeUnit.DAYS.toMillis(1)

        val stats =
            usageStatsManager.queryUsageStats(

                UsageStatsManager
                    .INTERVAL_DAILY,

                startTime,
                endTime
            )

        return stats.sumOf {

            it.totalTimeInForeground
        } / 1000 / 60
    }

    fun getMostUsedApp(
        context: Context
    ): String {

        val usageList =
            UsageStatsHelper
                .getUsageStats(context)

        return usageList
            .firstOrNull()
            ?.appName
            ?: "Unknown"
    }
}