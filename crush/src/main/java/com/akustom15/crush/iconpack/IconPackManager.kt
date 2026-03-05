package com.akustom15.crush.iconpack

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.XmlResourceParser
import android.util.Log
import android.widget.Toast
import com.akustom15.crush.R
import org.xmlpull.v1.XmlPullParser

data class AppCounters(
    val totalApps: Int = 0,
    val themedApps: Int = 0,
    val missingApps: Int = 0
)

data class LauncherInfo(
    val name: String,
    val packageName: String,
    val intentAction: String,
    val extraKey: String = "package"
)

object IconPackManager {

    private const val TAG = "IconPackManager"

    private val SUPPORTED_LAUNCHERS = listOf(
        LauncherInfo("Nova Launcher", "com.teslacoilsw.launcher", "com.teslacoilsw.launcher.APPLY_ICON_THEME"),
        LauncherInfo("Action Launcher", "com.actionlauncher.playstore", "com.actionlauncher.applytheme", "iconpack"),
        LauncherInfo("Apex Launcher", "com.anddoes.launcher", "com.anddoes.launcher.SET_THEME", "com.anddoes.launcher.THEME_PACKAGE_NAME"),
        LauncherInfo("ADW Launcher", "org.adw.launcher", "org.adw.launcher.SET_THEME"),
        LauncherInfo("Smart Launcher", "ginlemon.flowerfree", "ginlemon.smartlauncher.setGSLTHEME", "package"),
        LauncherInfo("Smart Launcher", "ginlemon.smartlauncher", "ginlemon.smartlauncher.setGSLTHEME", "package"),
        LauncherInfo("Lawnchair", "ch.deletescape.lawnchair.plah", "ch.deletescape.lawnchair.APPLY_ICONS"),
        LauncherInfo("Lawnchair 12+", "app.lawnchair", "app.lawnchair.APPLY_ICONS"),
        LauncherInfo("POCO Launcher", "com.mi.android.globallauncher", "com.mi.android.globallauncher.APPLY_ICONS"),
        LauncherInfo("Microsoft Launcher", "com.microsoft.launcher", "com.microsoft.launcher.action.APPLY_ICON_THEME", "com.microsoft.launcher.iconpack.ICON_THEME_PACKAGE"),
        LauncherInfo("Niagara Launcher", "bitpit.launcher", "bitpit.launcher.APPLY_ICONS"),
        LauncherInfo("Total Launcher", "com.ss.launcher2", "com.ss.launcher2.APPLY_THEME"),
        LauncherInfo("Hyperion", "projekt.launcher", "projekt.launcher.APPLY_ICON_THEME")
    )

    fun getCompatibleLaunchers(context: Context): List<LauncherInfo> {
        val pm = context.packageManager

        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val homeResolveInfos = pm.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val installedPkgs = homeResolveInfos.map { it.activityInfo.packageName }.toMutableSet()

        SUPPORTED_LAUNCHERS.forEach { launcher ->
            if (!installedPkgs.contains(launcher.packageName)) {
                try {
                    pm.getPackageInfo(launcher.packageName, 0)
                    installedPkgs.add(launcher.packageName)
                } catch (_: PackageManager.NameNotFoundException) {}
            }
        }

        return SUPPORTED_LAUNCHERS.filter { installedPkgs.contains(it.packageName) }
    }

    fun applyIconPack(context: Context, launcher: LauncherInfo): Boolean {
        return when (launcher.packageName) {
            "com.teslacoilsw.launcher" -> applyToNovaLauncher(context, launcher)
            "com.microsoft.launcher" -> applyToMicrosoftLauncher(context, launcher)
            "com.anddoes.launcher" -> applyToApexLauncher(context, launcher)
            "ch.deletescape.lawnchair.plah", "ch.deletescape.lawnchair", "app.lawnchair" -> applyToLawnchairLauncher(context, launcher)
            "ginlemon.flowerfree", "ginlemon.smartlauncher", "ginlemon.flower.launcher" -> applyToSmartLauncher(context, launcher)
            "com.actionlauncher.playstore" -> applyToActionLauncher(context, launcher)
            else -> applyGeneric(context, launcher)
        }
    }

    private fun applyToNovaLauncher(context: Context, launcher: LauncherInfo): Boolean {
        val iconPackPkg = context.packageName
        val attempts = listOf(
            {
                Intent("com.teslacoilsw.launcher.APPLY_ICON_THEME").apply {
                    putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_TYPE", "GO")
                    putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE", iconPackPkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            },
            {
                Intent("com.teslacoilsw.launcher.APPLY_ICON_THEME").apply {
                    putExtra("com.teslacoilsw.launcher.extra.ICON_PACKAGE", iconPackPkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            },
            {
                Intent(Intent.ACTION_MAIN).apply {
                    component = ComponentName(launcher.packageName, "com.teslacoilsw.launcher.NovaLauncher")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        )
        for (gen in attempts) {
            try {
                context.startActivity(gen())
                Toast.makeText(context, context.getString(R.string.launcher_applying, "Nova Launcher"), Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Nova attempt failed: ${e.message}")
            }
        }
        Toast.makeText(context, context.getString(R.string.launcher_not_found), Toast.LENGTH_LONG).show()
        return false
    }

    private fun applyToMicrosoftLauncher(context: Context, launcher: LauncherInfo): Boolean {
        val iconPackPkg = context.packageName
        val intent = Intent("com.microsoft.launcher.action.APPLY_ICON_THEME").apply {
            setPackage(launcher.packageName)
            putExtra("com.microsoft.launcher.iconpack.ICON_THEME_PACKAGE", iconPackPkg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.launcher_applying, "Microsoft Launcher"), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Microsoft Launcher failed: ${e.message}")
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(launcher.packageName)
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent?.let { context.startActivity(it) }
                true
            } catch (e2: Exception) {
                Toast.makeText(context, context.getString(R.string.launcher_not_found), Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    private fun applyToApexLauncher(context: Context, launcher: LauncherInfo): Boolean {
        val intent = Intent("com.anddoes.launcher.SET_THEME").apply {
            putExtra("com.anddoes.launcher.THEME_PACKAGE_NAME", context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.launcher_applying, "Apex Launcher"), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.launcher_not_found), Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun applyToLawnchairLauncher(context: Context, launcher: LauncherInfo): Boolean {
        val action = if (launcher.packageName == "app.lawnchair") "app.lawnchair.APPLY_ICONS" else "ch.deletescape.lawnchair.APPLY_ICONS"
        val intent = Intent(action).apply {
            setPackage(launcher.packageName)
            putExtra("packageName", context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.launcher_applying, "Lawnchair"), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.launcher_not_found), Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun applyToSmartLauncher(context: Context, launcher: LauncherInfo): Boolean {
        val intent = Intent("ginlemon.smartlauncher.SET_THEME").apply {
            setPackage(launcher.packageName)
            putExtra("package", context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.launcher_applying, "Smart Launcher"), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Smart Launcher SET_THEME failed: ${e.message}")
            val themesIntent = Intent("ginlemon.smartlauncher.THEMES").apply {
                setPackage(launcher.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(themesIntent)
                true
            } catch (e2: Exception) {
                Toast.makeText(context, context.getString(R.string.launcher_not_found), Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    private fun applyToActionLauncher(context: Context, launcher: LauncherInfo): Boolean {
        val intent = Intent("com.actionlauncher.THEME").apply {
            setPackage(launcher.packageName)
            putExtra("iconpack", context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            Toast.makeText(context, context.getString(R.string.launcher_applying, "Action Launcher"), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.launcher_not_found), Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun applyGeneric(context: Context, launcher: LauncherInfo): Boolean {
        if (launcher.intentAction.isNotBlank() && launcher.extraKey.isNotBlank()) {
            try {
                val intent = Intent(launcher.intentAction).apply {
                    setPackage(launcher.packageName)
                    putExtra(launcher.extraKey, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Toast.makeText(context, context.getString(R.string.launcher_applying, launcher.name), Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Generic apply failed for ${launcher.name}: ${e.message}")
            }
        }
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(launcher.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback open launcher failed for ${launcher.name}: ${e.message}")
        }
        Toast.makeText(context, context.getString(R.string.launcher_not_found), Toast.LENGTH_LONG).show()
        return false
    }

    fun getAppCounters(context: Context): AppCounters {
        return try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val allApps: List<ResolveInfo> = pm.queryIntentActivities(mainIntent, 0)
            val totalApps = allApps.size

            val themedComponents = loadThemedComponents(context)
            var themedCount = 0
            for (app in allApps) {
                val componentName = "${app.activityInfo.packageName}/${app.activityInfo.name}"
                if (themedComponents.contains(componentName.lowercase())) {
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
                                    val cleaned = component
                                        .removePrefix("ComponentInfo{")
                                        .removeSuffix("}")
                                    components.add(cleaned.lowercase())
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
