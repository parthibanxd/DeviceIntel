package com.example.deviceintel

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.roundToInt

data class RamMetrics(val usedGb: Float, val totalGb: Float, val percentage: Float)
data class StorageMetrics(val usedGb: Long, val totalGb: Long, val percentage: Float)

fun getLiveBatteryPercentage(context: Context): Float {
    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    return if (level >= 0 && scale > 0) level / scale.toFloat() else 0.5f
}

fun getLiveBatteryTemperature(context: Context): Float {
    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    return (batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
}

fun getBatteryCapacityMah(context: Context): Int {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    val pct = getLiveBatteryPercentage(context)

    if (chargeCounterUah > 0 && pct > 0f) {
        val estimated = ((chargeCounterUah / 1000f) / pct).toInt()
        if (estimated in 2000..7500) return estimated
    }

    try {
        val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
        val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
        val batteryCapacity = powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile) as Double
        if (batteryCapacity > 0) return batteryCapacity.toInt()
    } catch (_: Exception) {}

    return 5000
}

fun getLiveRamMetrics(context: Context): RamMetrics {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)

    val totalGbRaw = memoryInfo.totalMem.toFloat() / (1024 * 1024 * 1024)
    val availGbRaw = memoryInfo.availMem.toFloat() / (1024 * 1024 * 1024)
    val usedGbRaw = totalGbRaw - availGbRaw

    return RamMetrics(
        usedGb = ((usedGbRaw * 10).roundToInt() / 10.0f),
        totalGb = ((totalGbRaw * 10).roundToInt() / 10.0f),
        percentage = (usedGbRaw / totalGbRaw).coerceIn(0f, 1f)
    )
}

fun getLiveStorageMetrics(): StorageMetrics {
    val stat = StatFs(Environment.getDataDirectory().path)
    val totalBytes = stat.blockCountLong * stat.blockSizeLong
    val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
    val usedBytes = totalBytes - freeBytes

    val totalGb = totalBytes / (1024 * 1024 * 1024)
    val usedGb = usedBytes / (1024 * 1024 * 1024)

    return StorageMetrics(
        usedGb = usedGb,
        totalGb = totalGb,
        percentage = if (totalGb > 0) (usedGb.toFloat() / totalGb.toFloat()).coerceIn(0f, 1f) else 0f
    )
}

fun getChipsetName(): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val socModel = Build.SOC_MODEL
        if (socModel.isNotBlank() && socModel != "unknown") return socModel
    }
    val hardware = Build.HARDWARE.uppercase()
    return when (hardware) {
        "SUN" -> "Snapdragon 8 Elite"
        "PINEAPPLE" -> "Snapdragon 8 Gen 3"
        "KALAMA" -> "Snapdragon 8 Gen 2"
        "TARO" -> "Snapdragon 8 Gen 1"
        "LAHAINA" -> "Snapdragon 888"
        "KONA" -> "Snapdragon 865"
        else -> hardware
    }
}

fun getCustomUIVersion(): String {
    val manufacturer = Build.MANUFACTURER.lowercase()
    val brand = Build.BRAND.lowercase()
    return try {
        val sysProp = Class.forName("android.os.SystemProperties")
        val getMethod = sysProp.getMethod("get", String::class.java)

        val hyperOs = getMethod.invoke(null, "ro.miui.os.version.name") as? String
        if (!hyperOs.isNullOrBlank()) return "HyperOS $hyperOs"

        val miui = getMethod.invoke(null, "ro.miui.ui.version.name") as? String
        if (!miui.isNullOrBlank()) return "MIUI $miui"

        val colorOs = getMethod.invoke(null, "ro.build.version.oplusrom") as? String
        if (!colorOs.isNullOrBlank()) return "ColorOS $colorOs"

        val oxygenOs = getMethod.invoke(null, "ro.oxygen.version") as? String
        if (!oxygenOs.isNullOrBlank()) return "OxygenOS $oxygenOs"

        val funtouch = getMethod.invoke(null, "ro.vivo.os.version") as? String
        if (!funtouch.isNullOrBlank()) return "FuntouchOS $funtouch"

        val realmeUi = getMethod.invoke(null, "ro.realme.ui.version") as? String
        if (!realmeUi.isNullOrBlank()) return "Realme UI $realmeUi"

        if (manufacturer.contains("samsung") || brand.contains("samsung")) {
            val oneUi = getMethod.invoke(null, "ro.build.version.oneui") as? String
            if (!oneUi.isNullOrBlank()) return "One UI $oneUi"
            return "One UI"
        }
        if (manufacturer.contains("huawei") || brand.contains("huawei")) return "EMUI"
        if (manufacturer.contains("transsion") || manufacturer.contains("infinix") || manufacturer.contains("tecno")) return "HiOS / XOS"
        if (manufacturer.contains("google") || manufacturer.contains("motorola")) return "Pixel / Moto UX"

        "AOSP / Stock Android"
    } catch (e: Exception) {
        "AOSP / Stock Android"
    }
}

@Composable
fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF8B8B99), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, color = Color(0xFF8B8B99), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = value, color = Color(0xFFF3F4F6), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
