package com.akustom15.crush.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.XmlResourceParser
import android.util.Log
import org.xmlpull.v1.XmlPullParser

data class AppCounters(
    val totalApps: Int = 0,
    val themedApps: Int = 0,
    val missingApps: Int = 0
)

data class LauncherInfo(
    val name: String,
    val packageName: String,
    val action: String
)

object IconPackManager {

    private const val TAG = "IconPackManager"

    private val SUPPORTED_LAUNCHERS = listOf(
        LauncherInfo("Nova Launcher", "com.teslacoilsw.launcher", "com.teslacoilsw.launcher.APPLY_ICON_THEME"),
        LauncherInfo("Smart Launcher", "ginlemon.flowerfree", "ginlemon.flowerfree.APPLY_ICON_THEME"),
        LauncherInfo("Smart Launcher Pro", "ginlemon.flowerpro", "ginlemon.flowerpro.APPLY_ICON_THEME"),
        LauncherInfo("Action Launcher", "com.actionlauncher.playstore", "com.actionlauncher.playstore.ACTION_APPLY_ICON_THEME"),
        LauncherInfo("Lawnchair", "ch.deletescape.lawnchair.plah", "ch.deletescape.lawnchair.plah.APPLY_ICON_THEME"),
        LauncherInfo("Niagara Launcher", "bitpit.launcher", "bitpit.launcher.APPLY_ICON_THEME"),
        LauncherInfo("Hyperion", "projekt.launcher", "projekt.launcher.APPLY_ICON_THEME"),
        LauncherInfo("Apex Launcher", "com.anddoes.launcher", "com.anddoes.launcher.APPLY_ICON_THEME"),
        LauncherInfo("Evie Launcher", "is.shortcut", "is.shortcut.APPLY_ICON_THEME"),
        LauncherInfo("POCO Launcher", "com.mi.android.globallauncher", "com.mi.android.globallauncher.APPLY_ICON_THEME"),
        LauncherInfo("Microsoft Launcher", "com.microsoft.launcher", "com.microsoft.launcher.APPLY_ICON_THEME"),
        LauncherInfo("Customized Pixel Launcher", "app.flavor.launcher", "app.flavor.launcher.APPLY_ICON_THEME")
    )

    fun getCompatibleLaunchers(context: Context): List<LauncherInfo> {
        val pm = context.packageManager
        return SUPPORTED_LAUNCHERS.filter { launcher ->
            try {
                pm.getPackageInfo(launcher.packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    fun applyIconPack(context: Context, launcher: LauncherInfo) {
        try {
            val intent = Intent(launcher.action).apply {
                putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE", context.packageName)
                putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_TYPE", 1)
                putExtra("org.adw.launcher.THEMED_ICON_PACK", context.packageName)
                putExtra("packageName", context.packageName)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(launcher.packageName)
                launchIntent?.let { context.startActivity(it) }
            }
            Log.d(TAG, "Applied icon pack via ${launcher.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying icon pack to ${launcher.name}", e)
        }
    }

    fun showLauncherSelector(context: Context) {
        val compatible = getCompatibleLaunchers(context)
        if (compatible.size == 1) {
            applyIconPack(context, compatible.first())
        } else if (compatible.isNotEmpty()) {
            // Multiple launchers — handled by the UI via dialog
            Log.d(TAG, "Multiple launchers found: ${compatible.map { it.name }}")
        } else {
            Log.w(TAG, "No compatible launchers found")
        }
    }

    fun getAppCounters(context: Context): AppCounters {
        return try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val allApps: List<ResolveInfo> = pm.queryIntentActivities(mainIntent, 0)
            val totalApps = allApps.size

            // Load themed icons from appfilter
            val themedComponents = loadThemedComponents(context)
            var themedCount = 0
            for (app in allApps) {
                val componentName = "${app.activityInfo.packageName}/${app.activityInfo.name}"
                if (themedComponents.contains(componentName)) {
                    themedCount++
                }
            }

            AppCounters(
                totalApps = totalApps,
                themedApps = themedCount,
                missingApps = totalApps - themedCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating app counters", e)
            AppCounters()
        }
    }

    fun getTotalIconsCount(context: Context): Int {
        return try {
            loadAllDrawableNames(context).size
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total icons count", e)
            0
        }
    }

    private fun loadThemedComponents(context: Context): Set<String> {
        val components = mutableSetOf<String>()
        try {
            // Try appfilter_new first, then appfilter
            val xmlNames = listOf("appfilter_new", "appfilter")
            for (xmlName in xmlNames) {
                val resId = context.resources.getIdentifier(xmlName, "xml", context.packageName)
                if (resId != 0) {
                    val parser: XmlResourceParser = context.resources.getXml(resId)
                    var eventType = parser.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                            for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i) == "component") {
                                    val component = parser.getAttributeValue(i)
                                    // Format: ComponentInfo{pkg/class}
                                    val cleaned = component
                                        .removePrefix("ComponentInfo{")
                                        .removeSuffix("}")
                                    components.add(cleaned)
                                }
                            }
                        }
                        eventType = parser.next()
                    }
                    parser.close()
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading themed components", e)
        }
        return components
    }

    private fun loadAllDrawableNames(context: Context): Set<String> {
        val drawables = mutableSetOf<String>()
        try {
            val xmlNames = listOf("drawable", "drawable_new")
            for (xmlName in xmlNames) {
                val resId = context.resources.getIdentifier(xmlName, "xml", context.packageName)
                if (resId != 0) {
                    val parser: XmlResourceParser = context.resources.getXml(resId)
                    var eventType = parser.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                            for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i) == "drawable") {
                                    parser.getAttributeValue(i)?.let { drawables.add(it) }
                                }
                            }
                        }
                        eventType = parser.next()
                    }
                    parser.close()
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading drawable names", e)
        }
        return drawables
    }
}
