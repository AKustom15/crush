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

        // Inicializar notificaciones push
        CrushNotificationHelper.initialize(this)

        // Configurar la librería Crush — CAMBIA ESTOS VALORES PARA TU APP
        val crushConfig = CrushConfig(
            appName = "Crush Template",
            appSubtitle = "Beautiful Icon Pack for your device",
            appIcon = R.mipmap.ic_launcher,
            packageName = packageName,

            // Configuración de tabs
            showIconsTab = true,           // Mostrar tab de Iconos
            showWidgets = false,           // No hay archivos KWGT/KLWP en assets. Activar cuando agregues widgets.
            showWallpapers = false,        // Mostrar tab de Wallpapers locales
            showWallpaperCloud = true,     // Mostrar tab de Wallpapers en la nube
            widgetType = WidgetType.KWGT,  // KWGT o KLWP

            // URL de wallpapers en la nube (formato JSON)
            cloudWallpapersUrl = "https://raw.githubusercontent.com/rs1525/zyra_wall/refs/heads/main/wallpaper_zyra.json",

            // Configuración de solicitud de iconos
            iconRequestEmail = "your_email@example.com",
            freeIconRequestLimit = 10,
            enablePremiumRequest = false,

            // Información del desarrollador (pantalla About)
            developerName = "AKustom15",
            developerLogoUrl = "",
            moreAppsUrl = "https://play.google.com/store/apps/dev?id=YOUR_DEV_ID",
            privacyPolicyUrl = "",

            // URL para verificar actualizaciones
            updateJsonUrl = "",

            // Changelog
            changelog = ChangelogConfig(
                entries = listOf(
                    ChangelogEntry.fromText("Initial release"),
                    ChangelogEntry.fromText("Icon pack with 100+ icons")
                )
            ),

            // Texto del splash screen (máximo 3 partes)
            splashTextParts = listOf("Crush", "Icon", "Pack"),
            splashAnimationDurationMs = 3000L,

            // Iconos de redes sociales — proporciona tus propios drawables
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
