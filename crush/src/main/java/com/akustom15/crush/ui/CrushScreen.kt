package com.akustom15.crush.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.akustom15.crush.R
import com.akustom15.crush.config.CrushConfig
import com.akustom15.crush.config.CrushTab
import com.akustom15.crush.ui.components.CrushBottomNavigation
import com.akustom15.crush.ui.screens.dashboard.DashboardScreen
import com.akustom15.crush.ui.screens.wallpapers.CloudWallpaperScreen
import com.akustom15.crush.ui.screens.widgets.WidgetGridScreen
import com.akustom15.crush.ui.theme.CrushTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun CrushScreen(config: CrushConfig) {
    CrushTheme {
        CrushScreenContent(config)
    }
}

@Composable
private fun CrushScreenContent(config: CrushConfig) {
    val context = LocalContext.current
    val visibleTabs = remember(config) { config.getVisibleTabs() }
    var selectedTab by remember(visibleTabs) {
        mutableStateOf(visibleTabs.firstOrNull() ?: CrushTab.Dashboard)
    }

    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomContentPadding = if (visibleTabs.size > 1) navBarBottom + 80.dp else navBarBottom

    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
        ) {
            when (selectedTab) {
                CrushTab.Dashboard -> {
                    DashboardScreen(
                        config = config,
                        bottomContentPadding = bottomContentPadding
                    )
                }
                CrushTab.Widgets -> {
                    WidgetGridScreen(
                        config = config,
                        bottomContentPadding = bottomContentPadding
                    )
                }
                CrushTab.WallpaperCloud -> {
                    CloudWallpaperScreen(
                        config = config,
                        bottomContentPadding = bottomContentPadding
                    )
                }
            }
        }

        // Bottom navigation with gradient + pill
        if (visibleTabs.size > 1) {
            val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
            val navbarColor = if (isDark) {
                Color(ContextCompat.getColor(context, R.color.crush_navbar_color_dark))
            } else {
                Color(ContextCompat.getColor(context, R.color.crush_navbar_color_light))
            }
            val topbarHeight = 62.dp + 8.dp + navBarBottom

            // Gradient behind pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(topbarHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                navbarColor.copy(alpha = 0.80f)
                            )
                        )
                    )
            )

            // PIL with blur
            CrushBottomNavigation(
                visibleTabs = visibleTabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                hazeState = hazeState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
