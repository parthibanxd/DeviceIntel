package com.example.deviceintel

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.concurrent.TimeUnit

object UsageStatsHelper {

    private val blockedPackages = setOf(

        "com.android.systemui",

        "android",

        "com.google.android.permissioncontroller",

        "com.miui.home",

        "com.mi.android.globallauncher",

        "com.google.android.apps.nexuslauncher",

        "com.android.launcher",

        "com.android.launcher3",

        "com.miui.securitycenter",

        "com.google.android.gms",

        "com.google.android.gsf",

        "com.android.settings",

        "com.google.android.inputmethod.latin"
    )

    fun getUsageStats(
        context: Context
    ): List<AppUsageInfo> {

        val usageStatsManager =

            context.getSystemService(
                Context.USAGE_STATS_SERVICE
            ) as UsageStatsManager

        val packageManager =
            context.packageManager

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

        val appMap =
            mutableMapOf<String, AppUsageInfo>()

        for (usage in stats) {

            if (
                usage.totalTimeInForeground <= 0
            ) continue

            val packageName =
                usage.packageName

            if (
                blockedPackages.contains(
                    packageName
                )
            ) continue

            if (

                packageName.contains(
                    "launcher",
                    ignoreCase = true
                )
            ) continue

            if (

                packageName.contains(
                    "inputmethod",
                    ignoreCase = true
                )
            ) continue

            if (

                packageName.contains(
                    "systemui",
                    ignoreCase = true
                )
            ) continue

            try {

                val appInfo =
                    packageManager
                        .getApplicationInfo(
                            packageName,
                            0
                        )

                val appName =
                    packageManager
                        .getApplicationLabel(
                            appInfo
                        ).toString()

                val icon =
                    packageManager
                        .getApplicationIcon(
                            appInfo
                        )

                val usageMinutes =
                    usage.totalTimeInForeground /
                            1000 / 60

                if (usageMinutes < 1)
                    continue

                val existing =
                    appMap[packageName]

                if (

                    existing == null ||

                    usageMinutes >
                    existing.usageMinutes
                ) {

                    appMap[packageName] =

                        AppUsageInfo(

                            appName =
                                appName,

                            packageName =
                                packageName,

                            usageMinutes =
                                usageMinutes,

                            icon = icon
                        )
                }

            } catch (_: Exception) {
            }
        }

        return appMap.values
            .sortedByDescending {

                it.usageMinutes
            }
            .take(20)
    }
}