package com.akustom15.crush.iconpack

import android.content.Context

/**
 * Manages favorite icons using SharedPreferences.
 * Icons are stored by their drawable name.
 */
object FavoriteIconsManager {
    private const val PREFS_NAME = "crush_favorite_icons"
    private const val KEY_FAVORITES = "favorites"

    fun loadFavoriteIcons(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavoriteIcon(context: Context, iconName: String): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentFavorites = prefs.getStringSet(KEY_FAVORITES, emptySet())?.toMutableSet() ?: mutableSetOf()

        if (currentFavorites.contains(iconName)) {
            currentFavorites.remove(iconName)
        } else {
            currentFavorites.add(iconName)
        }

        prefs.edit().putStringSet(KEY_FAVORITES, currentFavorites).apply()
        return currentFavorites.toSet()
    }

    fun isFavorite(context: Context, iconName: String): Boolean {
        return loadFavoriteIcons(context).contains(iconName)
    }
}
