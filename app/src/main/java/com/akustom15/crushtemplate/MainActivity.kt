package com.akustom15.crushtemplate

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.akustom15.crush.config.ChangelogConfig
import com.akustom15.crush.config.ChangelogEntry
import com.akustom15.crush.config.CrushConfig
import com.akustom15.crush.config.WidgetType
import com.akustom15.crush.notifications.CrushNotificationHelper
import com.akustom15.crush.ui.CrushScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize push notifications
        CrushNotificationHelper.initialize(this)

        // Configure the Crush library — CHANGE THESE VALUES FOR YOUR APP
        val crushConfig = CrushConfig(
            appName = "Crush Template",
            appSubtitle = "Beautiful Icon Pack for your device",
            appIcon = R.mipmap.ic_launcher,
            packageName = packageName,

            // Tabs configuration
            showWidgets = true,           // Show Widgets/KLWP tab
            showWallpaperCloud = true,     // Show Cloud Wallpapers tab
            widgetType = WidgetType.KWGT,  // KWGT or KLWP

            // Cloud wallpapers URL (JSON format)
            cloudWallpapersUrl = "",

            // appfilter XML resource for icon animation on dashboard
            // Set to R.xml.appfilter if you have one
            appFilterXmlRes = 0,

            // Developer info
            developerName = "AKustom15",
            moreAppsUrl = "https://play.google.com/store/apps/dev?id=YOUR_DEV_ID",
            privacyPolicyUrl = "",

            // Update checker URL
            updateJsonUrl = "",

            // Changelog
            changelog = ChangelogConfig(
                entries = listOf(
                    ChangelogEntry.fromText("Initial release"),
                    ChangelogEntry.fromText("Icon pack with 100+ icons")
                )
            ),

            // Splash screen text (max 3 parts)
            splashTextParts = listOf("Crush", "Icon", "Pack"),
            splashAnimationDurationMs = 3000L,

            // Social media icons — provide your own drawables
            // xIcon = R.drawable.x,
            // instagramIcon = R.drawable.instagram,
            // youtubeIcon = R.drawable.youtube,
            // facebookIcon = R.drawable.facebook,
            // telegramIcon = R.drawable.telegram
        )

        setContent {
            CrushScreen(config = crushConfig)
        }
    }
}
