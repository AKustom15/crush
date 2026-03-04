package com.akustom15.crush.config

import androidx.annotation.DrawableRes

/**
 * Clase principal de configuración de la librería Crush.
 * Todo es configurable desde la app que consume la librería.
 */
data class CrushConfig(
    // Información básica de la app
    val appName: String,
    val appSubtitle: String = "",
    @DrawableRes val appIcon: Int? = null,
    val packageName: String,

    // Visibilidad de tabs en la navegación inferior
    val showIconsTab: Boolean = true,
    val showWidgets: Boolean = false,
    val showWallpapers: Boolean = false,
    val showWallpaperCloud: Boolean = false,
    val widgetType: WidgetType = WidgetType.KWGT,

    // URLs de recursos remotos
    val cloudWallpapersUrl: String = "",
    val updateJsonUrl: String = "",

    // Configuración de icon pack
    val iconRequestEmail: String = "",
    val freeIconRequestLimit: Int = 10,
    val enablePremiumRequest: Boolean = false,

    // Información del desarrollador (pantalla About)
    val developerLogoUrl: String = "",
    val developerName: String = "AKustom15",
    val moreAppsUrl: String = "",
    val moreApps: List<MoreApp> = emptyList(),
    val moreAppsJsonUrl: String = "",
    val privacyPolicyUrl: String = "",

    // Changelog
    val changelog: ChangelogConfig = ChangelogConfig(),

    // Splash screen
    val splashAnimationDurationMs: Long = 3000L,
    val splashTextParts: List<String> = emptyList(),

    // Iconos de redes sociales (pantalla About)
    @DrawableRes val xIcon: Int = android.R.drawable.ic_menu_send,
    @DrawableRes val instagramIcon: Int = android.R.drawable.ic_menu_camera,
    @DrawableRes val youtubeIcon: Int = android.R.drawable.ic_media_play,
    @DrawableRes val facebookIcon: Int = android.R.drawable.ic_menu_share,
    @DrawableRes val telegramIcon: Int = android.R.drawable.ic_menu_send
) {
    /** Obtener la lista de tabs visibles según la configuración */
    fun getVisibleTabs(): List<CrushTab> {
        return buildList {
            add(CrushTab.Dashboard)
            if (showIconsTab) add(CrushTab.Icons)
            if (showWidgets) add(CrushTab.Widgets)
            if (showWallpapers) add(CrushTab.Wallpapers)
            if (showWallpaperCloud) add(CrushTab.WallpaperCloud)
        }
    }
}

/** Tabs disponibles en la navegación inferior */
enum class CrushTab {
    Dashboard,
    Icons,
    Widgets,
    Wallpapers,
    WallpaperCloud
}

/** Tipo de widget Kustom */
enum class WidgetType {
    KWGT,
    KLWP
}

/** Modelo para "Más apps" en la pantalla About */
data class MoreApp(
    val name: String,
    val description: String = "",
    val iconUrl: String = "",
    val playStoreUrl: String = ""
)
