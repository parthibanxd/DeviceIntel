package com.example.deviceintel.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Local data structure to cleanly pair routes with display elements without triggering compilation exceptions
private data class TabMetadata(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    // Explicitly defines metadata associations to guarantee safe UI lookups
    val navigationTabs = remember {
        listOf(
            TabMetadata(Screen.Home.route, "Home", Icons.Default.Dashboard),
            TabMetadata(Screen.Analytics.route, "Analytics", Icons.Default.Analytics),
            TabMetadata(Screen.Apps.route, "Apps", Icons.Default.BarChart),
            TabMetadata(Screen.Insights.route, "Insights", Icons.Default.Insights)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 12.dp), // Fixed structural modifier spacing
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF161922)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationTabs.forEachIndexed { index, tab ->
                val selected = selectedIndex == index

                // Fluid hardware-accelerated color and structural transformations
                val animatedBgColor by animateColorAsState(
                    targetValue = if (selected) Color(0xFF1E457E) else Color.Transparent,
                    animationSpec = tween(durationMillis = 250),
                    label = "TabBackground"
                )

                val animatedContentColor by animateColorAsState(
                    targetValue = if (selected) Color(0xFF6EA8FF) else Color(0xFF8A93A6),
                    animationSpec = tween(durationMillis = 200),
                    label = "TabContent"
                )

                val animatedCornerRadius by animateDpAsState(
                    targetValue = if (selected) 24.dp else 30.dp,
                    animationSpec = tween(durationMillis = 250),
                    label = "TabCorners"
                )

                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(animatedCornerRadius))
                        .background(animatedBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!selected) onTabSelected(index)
                        }
                        .padding(horizontal = if (selected) 18.dp else 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Smoothly animated circular icon backdrop for active view selection
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (selected) Color(0xFF6EA8FF).copy(alpha = 0.2f) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = animatedContentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Expands the textual pill layout smoothly during focus shifts
                        AnimatedVisibility(visible = selected) {
                            Row {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tab.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}