package com.example.deviceintel

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deviceintel.navigation.BottomNavBar
import com.example.deviceintel.navigation.Screen
import com.example.deviceintel.ui.theme.DeviceIntelTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeviceIntelTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0A0A0F)) {
                    AppGatekeeper()
                }
            }
        }
    }
}

// --- App Gatekeeper: Decides whether to show Setup or the Main App ---
@Composable
fun AppGatekeeper() {
    val context = androidx.compose.ui.platform.LocalContext.current

    // State to track if the user has completed onboarding/permissions
    var isSetupComplete by remember { mutableStateOf(hasUsageAccess(context)) }

    if (!isSetupComplete) {
        // Show the sleek onboarding screen
        SetupScreen(
            onPermissionGranted = {
                isSetupComplete = true
            }
        )
    } else {
        // If permission is granted, boot up the main application
        RootNavigation()
    }
}

// Reusable permission checker for the Gatekeeper
@Suppress("DEPRECATION")
private fun hasUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    } else {
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

// --- Root Navigation (Handles Detail Screens sliding over the Bottom Bar) ---
@Composable
fun RootNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main_pager_screen",
        modifier = Modifier.background(Color(0xFF0A0A0F))
    ) {
        composable("main_pager_screen") {
            MainPagerScreen(navController = navController)
        }
        composable(Screen.AppDetail.route) {
            AppDetailScreen(navController = navController)
        }
        composable("battery_detail") {
            BatteryDetailScreen(onBack = { navController.popBackStack() })
        }
        composable("ram_detail") {
            RamDetailScreen(onBack = { navController.popBackStack() })
        }
        composable("thermal_detail") {
            ThermalDetailScreen(onBack = { navController.popBackStack() })
        }
        composable("storage_detail") {
            StorageDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}

// --- The Swipeable Pager Screen ---
@Composable
fun MainPagerScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        },
        containerColor = Color(0xFF0A0A0F)
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            beyondViewportPageCount = 3
        ) { page ->
            when (page) {
                0 -> HomeScreen(navController = navController)
                1 -> AnalyticsScreen(navController = navController)
                2 -> AppsScreen(navController = navController)
                3 -> InsightsScreen(navController = navController)
            }
        }
    }
}