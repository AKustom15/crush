package com.akustom15.crush.ui

import android.content.pm.PackageManager
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.akustom15.crush.R
import com.akustom15.crush.config.CrushConfig
import com.akustom15.crush.config.CrushTab
import com.akustom15.crush.iconpack.AppFilterParser
import com.akustom15.crush.ui.components.ChangelogDialog
import com.akustom15.crush.ui.components.CrushBottomNavigation
import com.akustom15.crush.ui.screens.about.AboutScreen
import com.akustom15.crush.ui.screens.dashboard.DashboardScreen
import com.akustom15.crush.ui.screens.icons.IconGridScreen
import com.akustom15.crush.ui.screens.request.IconRequestScreen
import com.akustom15.crush.ui.screens.settings.SettingsScreen
import com.akustom15.crush.ui.screens.wallpapers.CloudWallpaperScreen
import com.akustom15.crush.ui.screens.widgets.WidgetGridScreen
import com.akustom15.crush.ui.theme.CrushTheme
import com.akustom15.crush.utils.AssetsReader
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

/**
 * Pantalla principal de la librería Crush.
 * Integra búsqueda, menú, navegación inferior con blur, y todas las pantallas.
 */
@Composable
fun CrushScreen(config: CrushConfig) {
    CrushTheme {
        LocalizedContent {
            CrushScreenContent(config)
        }
    }
}

@Composable
private fun CrushScreenContent(config: CrushConfig) {
    val context = LocalContext.current
    val visibleTabs = remember(config) { config.getVisibleTabs() }
    var selectedTab by remember(visibleTabs) {
        mutableStateOf(visibleTabs.firstOrNull() ?: CrushTab.Dashboard)
    }
    // Estado de diálogos
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showIconRequestDialog by remember { mutableStateOf(false) }

    // Versión de la app detectada automáticamente
    val appVersion = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName}"
        } catch (e: PackageManager.NameNotFoundException) {
            "v1.0"
        }
    }

    // Contadores para el changelog
    val iconCount = remember(config.packageName) {
        try { AppFilterParser.getIconCount(context) } catch (e: Exception) { 0 }
    }
    val widgetCount = remember(config.packageName) {
        try { AssetsReader.getWidgetsFromAssets(context).size } catch (e: Exception) { 0 }
    }
    val wallpaperCount = remember(config.packageName) {
        try { AssetsReader.getWallpapersFromAssets(context).size } catch (e: Exception) { 0 }
    }

    // Padding inferior para el contenido (espacio para la pill de navegación)
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomContentPadding = if (visibleTabs.size > 1) navBarBottom + 80.dp else navBarBottom

    // Estado de Haze para blur en la pill de navegación
    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Contenido principal con blur para la pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .haze(state = hazeState)
            ) {
                when (selectedTab) {
                    CrushTab.Dashboard -> {
                        DashboardScreen(
                            config = config,
                            bottomContentPadding = bottomContentPadding,
                            onAboutClick = { showAboutDialog = true },
                            onSettingsClick = { showSettingsDialog = true },
                            onWallpapersClick = {
                                val wallpaperTab = visibleTabs.find {
                                    it == CrushTab.WallpaperCloud || it == CrushTab.Wallpapers
                                }
                                wallpaperTab?.let { selectedTab = it }
                            },
                            onIconRequestClick = { showIconRequestDialog = true }
                        )
                    }
                    CrushTab.Icons -> {
                        IconGridScreen(
                            config = config,
                            searchQuery = "",
                            showHeader = true,
                            bottomContentPadding = bottomContentPadding
                        )
                    }
                    CrushTab.Widgets -> {
                        WidgetGridScreen(
                            config = config,
                            bottomContentPadding = bottomContentPadding
                        )
                    }
                    CrushTab.Wallpapers -> {
                        CloudWallpaperScreen(
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
        }

        // Navegación inferior flotante con gradiente + pill con blur
        if (visibleTabs.size > 1) {
            val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
            val navbarColor = if (isDark) {
                Color(ContextCompat.getColor(context, R.color.crush_navbar_color_dark))
            } else {
                Color(ContextCompat.getColor(context, R.color.crush_navbar_color_light))
            }
            val topbarHeight = 62.dp + 8.dp + navBarBottom

            // Gradiente detrás de la pill
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

            // Pill con blur (Haze)
            CrushBottomNavigation(
                visibleTabs = visibleTabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                hazeState = hazeState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Diálogo de Changelog
    if (showChangelogDialog) {
        ChangelogDialog(
            changelog = config.changelog,
            appVersion = appVersion,
            iconCount = iconCount,
            widgetCount = widgetCount,
            wallpaperCount = wallpaperCount,
            onDismiss = { showChangelogDialog = false }
        )
    }

    // Pantalla About (diálogo a pantalla completa)
    if (showAboutDialog) {
        Dialog(
            onDismissRequest = { showAboutDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            AboutScreen(
                appIcon = config.appIcon,
                developerLogoUrl = config.developerLogoUrl,
                developerName = config.developerName,
                moreAppsUrl = config.moreAppsUrl,
                moreApps = config.moreApps,
                privacyPolicyUrl = config.privacyPolicyUrl,
                xIcon = config.xIcon,
                instagramIcon = config.instagramIcon,
                youtubeIcon = config.youtubeIcon,
                facebookIcon = config.facebookIcon,
                telegramIcon = config.telegramIcon,
                onNavigateBack = { showAboutDialog = false }
            )
        }
    }

    // Pantalla Icon Request (diálogo a pantalla completa)
    if (showIconRequestDialog && config.iconRequestEmail.isNotEmpty()) {
        Dialog(
            onDismissRequest = { showIconRequestDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            IconRequestScreen(
                config = config,
                onNavigateBack = { showIconRequestDialog = false }
            )
        }
    }

    // Pantalla Settings (diálogo a pantalla completa)
    if (showSettingsDialog) {
        Dialog(
            onDismissRequest = { showSettingsDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            SettingsScreen(
                packageName = config.packageName,
                appVersion = appVersion,
                updateJsonUrl = config.updateJsonUrl,
                onNavigateBack = { showSettingsDialog = false }
            )
        }
    }
}
