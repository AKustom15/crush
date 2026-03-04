package com.akustom15.crush.config

import androidx.annotation.DrawableRes

/**
 * Configuración de enlaces de redes sociales para la pantalla About.
 */
data class SocialMediaLink(
    val name: String,
    @DrawableRes val iconRes: Int,
    val url: String
)

object SocialMediaConfig {
    // URLs de redes sociales predeterminadas del desarrollador
    private const val X_URL = "https://twitter.com/akustom15"
    private const val INSTAGRAM_URL = "https://instagram.com/akustom15"
    private const val YOUTUBE_URL = "https://youtube.com/@akustom15"
    private const val FACEBOOK_URL = "https://facebook.com/akustom15"
    private const val TELEGRAM_URL = "https://t.me/akustom15"

    /** Obtener la lista de enlaces de redes sociales con iconos personalizados */
    fun getSocialMediaLinks(
        @DrawableRes xIcon: Int,
        @DrawableRes instagramIcon: Int,
        @DrawableRes youtubeIcon: Int,
        @DrawableRes facebookIcon: Int,
        @DrawableRes telegramIcon: Int
    ): List<SocialMediaLink> {
        return listOf(
            SocialMediaLink("X", xIcon, X_URL),
            SocialMediaLink("Instagram", instagramIcon, INSTAGRAM_URL),
            SocialMediaLink("YouTube", youtubeIcon, YOUTUBE_URL),
            SocialMediaLink("Facebook", facebookIcon, FACEBOOK_URL),
            SocialMediaLink("Telegram", telegramIcon, TELEGRAM_URL)
        )
    }
}
