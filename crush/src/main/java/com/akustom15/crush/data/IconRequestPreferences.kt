package com.akustom15.crush.data

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Manages icon request tracking via Firestore and local SharedPreferences.
 * Uses ANDROID_ID as device identifier.
 * Firestore collection: "icon_requests" / document: ANDROID_ID / field: "icons" (List<String>)
 */
object IconRequestPreferences {

    private const val PREF_NAME = "crush_icon_request_prefs"
    private const val KEY_PREMIUM_ENABLED = "premium_enabled"
    private const val KEY_PREMIUM_COUNT = "premium_count"
    private const val KEY_PREMIUM_TOTAL = "premium_total"
    private const val COLLECTION_NAME = "icon_requests"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    /**
     * Load already-requested icon package names from Firestore.
     */
    suspend fun loadRequestedIcons(context: Context): Set<String> {
        return try {
            val androidId = getDeviceId(context)
            val db = FirebaseFirestore.getInstance()
            val document = withContext(Dispatchers.IO) {
                db.collection(COLLECTION_NAME).document(androidId).get().await()
            }
            @Suppress("UNCHECKED_CAST")
            val icons = document.get("icons") as? List<String> ?: emptyList()
            icons.toSet()
        } catch (e: Exception) {
            Log.e("IconRequestPrefs", "Error loading from Firestore", e)
            emptySet()
        }
    }

    /**
     * Save updated requested icons to Firestore after a successful request.
     */
    suspend fun saveRequestedIcons(context: Context, icons: Set<String>): Boolean {
        return try {
            val androidId = getDeviceId(context)
            val db = FirebaseFirestore.getInstance()
            val data = mapOf("icons" to icons.toList())
            withContext(Dispatchers.IO) {
                db.collection(COLLECTION_NAME).document(androidId).set(data).await()
            }
            Log.d("IconRequestPrefs", "Saved ${icons.size} icons to Firestore")
            true
        } catch (e: Exception) {
            Log.e("IconRequestPrefs", "Error saving to Firestore", e)
            false
        }
    }

    // ========== Premium request tracking (local SharedPreferences) ==========

    fun isPremiumEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PREMIUM_ENABLED, false)
    }

    fun getPremiumCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_PREMIUM_COUNT, 0)
    }

    fun getPremiumTotal(context: Context): Int {
        return getPrefs(context).getInt(KEY_PREMIUM_TOTAL, 0)
    }

    fun consumePremiumRequests(context: Context, count: Int): Boolean {
        val prefs = getPrefs(context)
        val current = prefs.getInt(KEY_PREMIUM_COUNT, 0)
        if (current < count) return false
        prefs.edit().apply {
            putInt(KEY_PREMIUM_COUNT, current - count)
            if ((current - count) <= 0) putBoolean(KEY_PREMIUM_ENABLED, false)
            apply()
        }
        return true
    }

    fun addPremiumRequests(context: Context, count: Int) {
        val prefs = getPrefs(context)
        val current = prefs.getInt(KEY_PREMIUM_COUNT, 0)
        val total = prefs.getInt(KEY_PREMIUM_TOTAL, 0)
        prefs.edit().apply {
            putBoolean(KEY_PREMIUM_ENABLED, true)
            putInt(KEY_PREMIUM_COUNT, current + count)
            putInt(KEY_PREMIUM_TOTAL, total + count)
            apply()
        }
    }

    fun resetPremiumState(context: Context) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_PREMIUM_ENABLED, false)
            putInt(KEY_PREMIUM_COUNT, 0)
            putInt(KEY_PREMIUM_TOTAL, 0)
            apply()
        }
    }
}
