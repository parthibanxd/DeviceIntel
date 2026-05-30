package com.example.deviceintel.navigation

sealed class Screen(
    val route: String,
    val title: String
) {

    object Home :
        Screen(
            "home",
            "Dashboard"
        )

    object Analytics :
        Screen(
            "analytics",
            "Analytics"
        )

    object Apps :
        Screen(
            "apps",
            "Usage"
        )

    object Insights :
        Screen(
            "insights",
            "Reports"
        )

    object AppDetail :
        Screen(
            "app_detail",
            "App Analytics"
        )
}