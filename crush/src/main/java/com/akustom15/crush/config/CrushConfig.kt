package com.akustom15.crush.config

import androidx.annotation.DrawableRes

/**
 * Main configuration class for the Crush icon pack library.
 * Everything is configurable from the consuming app.
 *
 * @param appName Name of the icon pack app
 * @param appSubtitle Subtitle or tagline shown below the app name
 * @param appIcon Resource ID of the app icon (circular recommended)
 * @param packageName Package name of the consuming app
 * @param showWidgets Whether to show the Widgets/KLWP tab in bottom nav
 * @param showWallpaperCloud Whether to show the Cloud Wallpapers tab in bottom nav
 * @param widgetType Type of Kustom widgets: KWGT or KLWP
 * @param cloudWallpapersUrl URL to the JSON file containing cloud wallpapers data
 * @param appFilterXmlRes Resource ID for appfilter XML (icon pack mapping)
 * @param developerLogoUrl URL to the developer logo image (About screen)
 * @param developerName Developer name shown in About screen
 * @param moreAppsUrl URL to developer page on Play Store
 * @param privacyPolicyUrl URL to privacy policy page
 * @param updateJsonUrl URL to version JSON for update checking
 * @param changelog Changelog configuration for the changelog dialog
 * @param splashAnimationDurationMs Duration of splash animation before auto-navigating
 * @param splashTextParts Text parts for splash animation (max 3 parts)
 * @param xIcon Resource ID for Twitter/X social media icon
 * @param instagramIcon Resource ID for Instagram social media icon
 * @param youtubeIcon Resource ID for YouTube social media icon
 * @param facebookIcon Resource ID for Facebook social media icon
 * @param telegramIcon Resource ID for Telegram social media icon
 */
data class CrushConfig(
    val appName: String,
    val appSubtitle: String = "",
    @DrawableRes val appIcon: Int? = null,
    val packageName: String,
    val showWidgets: Boolean = true,
    val showWallpaperCloud: Boolean = true,
    val widgetType: WidgetType = WidgetType.KWGT,
    val cloudWallpapersUrl: String = "",
    val appFilterXmlRes: Int = 0,
    val developerLogoUrl: String = "",
    val developerName: String = "",
    val moreAppsUrl: String = "",
    val privacyPolicyUrl: String = "",
    val updateJsonUrl: String = "",
    val changelog: ChangelogConfig = ChangelogConfig(),
    val splashAnimationDurationMs: Long = 3000L,
    val splashTextParts: List<String> = emptyList(),
    @DrawableRes val xIcon: Int = android.R.drawable.ic_menu_send,
    @DrawableRes val instagramIcon: Int = android.R.drawable.ic_menu_camera,
    @DrawableRes val youtubeIcon: Int = android.R.drawable.ic_media_play,
    @DrawableRes val facebookIcon: Int = android.R.drawable.ic_menu_share,
    @DrawableRes val telegramIcon: Int = android.R.drawable.ic_menu_send
) {
    fun getVisibleTabs(): List<CrushTab> {
        return buildList {
            add(CrushTab.Dashboard)
            if (showWidgets) add(CrushTab.Widgets)
            if (showWallpaperCloud) add(CrushTab.WallpaperCloud)
        }
    }
}

enum class CrushTab {
    Dashboard,
    Widgets,
    WallpaperCloud
}

enum class WidgetType {
    KWGT,
    KLWP
}
