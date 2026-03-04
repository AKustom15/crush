package com.akustom15.crush.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

object KustomIntegration {

    private const val TAG = "KustomIntegration"
    private const val KWGT_PACKAGE = "org.kustom.widget"
    private const val KLWP_PACKAGE = "org.kustom.wallpaper"
    private const val KWGT_PRO_PACKAGE = "org.kustom.widget.pro"
    private const val KLWP_PRO_PACKAGE = "org.kustom.wallpaper.pro"

    fun applyWidget(context: Context, widgetFileName: String, packageName: String) {
        try {
            val uri = Uri.parse("kustom://widget/preset/$packageName/widgets/$widgetFileName")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                openPlayStore(context, KWGT_PACKAGE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying widget: $widgetFileName", e)
            openPlayStore(context, KWGT_PACKAGE)
        }
    }

    fun applyWallpaper(context: Context, wallpaperFileName: String, packageName: String) {
        try {
            val uri = Uri.parse("kustom://wallpaper/preset/$packageName/wallpapers/$wallpaperFileName")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                openPlayStore(context, KLWP_PACKAGE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying wallpaper: $wallpaperFileName", e)
            openPlayStore(context, KLWP_PACKAGE)
        }
    }

    fun isKwgtInstalled(context: Context): Boolean {
        return isPackageInstalled(context, KWGT_PACKAGE) || isPackageInstalled(context, KWGT_PRO_PACKAGE)
    }

    fun isKlwpInstalled(context: Context): Boolean {
        return isPackageInstalled(context, KLWP_PACKAGE) || isPackageInstalled(context, KLWP_PRO_PACKAGE)
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun openPlayStore(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
