package com.akustom15.crush.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.akustom15.crush.R
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class MissingApp(
    val name: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable? = null,
    var isSelected: Boolean = false
)

object IconRequestHelper {
    private const val TAG = "IconRequestHelper"

    fun getMissingIconApps(context: Context): List<MissingApp> {
        val missingApps = mutableListOf<MissingApp>()
        try {
            val themedComponents = getThemedComponentsFromAssets(context)
            val pm = context.packageManager
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            installedApps.forEach { appInfo ->
                try {
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    val isUserApp = !isSystemApp || isUpdatedSystem

                    if (isUserApp) {
                        val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                        if (launchIntent != null) {
                            val component = launchIntent.component
                            if (component != null) {
                                val componentKey = "${appInfo.packageName}/${component.className}".lowercase()
                                if (!themedComponents.contains(componentKey)) {
                                    val appLabel = appInfo.loadLabel(pm).toString()
                                    val icon = appInfo.loadIcon(pm)
                                    missingApps.add(
                                        MissingApp(
                                            name = appLabel,
                                            packageName = appInfo.packageName,
                                            activityName = component.className,
                                            icon = icon
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error processing app ${appInfo.packageName}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting missing icon apps", e)
        }
        return missingApps.sortedBy { it.name.lowercase() }
    }

    fun shareIconRequests(
        context: Context,
        selectedPackages: Set<String>,
        allApps: List<MissingApp>,
        email: String,
        appName: String,
        isPremium: Boolean
    ) {
        try {
            val appsToRequest = allApps.filter { selectedPackages.contains(it.packageName) }
            if (appsToRequest.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.icon_request_empty), Toast.LENGTH_SHORT).show()
                return
            }

            val cacheDir = context.cacheDir
            val payloadDir = File(cacheDir, "icon_request_payload").apply {
                if (exists()) deleteRecursively()
                mkdirs()
            }

            val appfilterXml = generateAppfilterXml(appsToRequest)
            File(payloadDir, "appfilter.xml").writeText(appfilterXml)

            val appmapXml = generateAppmapXml(appsToRequest)
            File(payloadDir, "appmap.xml").writeText(appmapXml)

            val iconsDir = File(payloadDir, "icons").apply { mkdirs() }
            exportAppIcons(context, appsToRequest, iconsDir)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val zipFile = File(cacheDir, "${appName.replace(" ", "_")}_icon_request_${timestamp}.zip")
            createZipFromDirectory(payloadDir, zipFile)

            if (!zipFile.exists() || zipFile.length() == 0L) {
                Toast.makeText(context, "Error creating request ZIP", Toast.LENGTH_SHORT).show()
                return
            }

            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", zipFile)

            val subject = if (isPremium) {
                "PREMIUM REQUEST (${appsToRequest.size} apps) - $appName"
            } else {
                context.getString(R.string.icon_request_email_subject, appName) + " (${appsToRequest.size} apps)"
            }

            var emailBody = if (isPremium) "[PREMIUM REQUEST]\n\n" else ""
            emailBody += "Icon request for $appName:\n\n"
            emailBody += appsToRequest.joinToString("\n\n") { app ->
                val playStoreLink = "https://play.google.com/store/apps/details?id=${app.packageName}"
                "• ${app.name} (${app.packageName})\n  Play Store: $playStoreLink"
            }
            emailBody += "\n\nDevice: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            emailBody += "\nAndroid: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})"
            emailBody += "\n\n(Full component details are in the attached XML files.)"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, emailBody)
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(
                    Intent.createChooser(shareIntent, "Send icon request")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error opening email app: ${e.message}")
                Toast.makeText(context, context.getString(R.string.icon_request_no_email), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing icon request", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun generateAppfilterXml(apps: List<MissingApp>): String {
        return buildString {
            append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            append("<resources>\n")
            append("<!-- Icon request: appfilter.xml -->\n")
            apps.forEach { app ->
                val drawableName = "icon_${normalizeName(app.name)}"
                val componentInfo = "ComponentInfo{${app.packageName}/${app.activityName}}"
                val escapedName = escapeXml(app.name)
                append("\n    <!-- $escapedName -->\n")
                append("    <item\n")
                append("        component=\"$componentInfo\"\n")
                append("        drawable=\"$drawableName\" />\n")
                append("    <item name=\"$escapedName\" drawable=\"$drawableName\" ")
                append("package=\"${app.packageName}\" activity=\"${app.activityName}\" />\n")
            }
            append("</resources>\n")
        }
    }

    private fun generateAppmapXml(apps: List<MissingApp>): String {
        return buildString {
            append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            append("<appmap>\n")
            append("    <!-- Icon request: appmap.xml -->\n")
            apps.forEach { app ->
                val drawableName = "icon_${normalizeName(app.name)}"
                val escapedName = escapeXml(app.name)
                append("    <item class=\"$escapedName\" name=\"$escapedName\" drawable=\"$drawableName\" />\n")
            }
            append("</appmap>\n")
        }
    }

    private fun exportAppIcons(context: Context, apps: List<MissingApp>, outputDir: File) {
        apps.forEach { app ->
            try {
                val drawable = app.icon ?: context.getDrawable(android.R.drawable.sym_def_app_icon) ?: return@forEach
                val baseBitmap = drawableToBitmap(drawable)
                val targetSize = 512
                val scaledBitmap = if (baseBitmap.width != targetSize || baseBitmap.height != targetSize) {
                    Bitmap.createScaledBitmap(baseBitmap, targetSize, targetSize, true)
                } else baseBitmap

                val fileName = "icon_${normalizeName(app.name)}.png"
                val outFile = File(outputDir, fileName)
                FileOutputStream(outFile).use { fos ->
                    BufferedOutputStream(fos).use { bos ->
                        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                        bos.flush()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not export icon for ${app.packageName}", e)
            }
        }
    }

    private fun createZipFromDirectory(sourceDir: File, zipFile: File) {
        fun addFileToZip(baseDir: File, file: File, zos: ZipOutputStream) {
            val entryName = file.relativeTo(baseDir).path.replace('\\', '/')
            if (file.isDirectory) {
                val children = file.listFiles()
                if (children.isNullOrEmpty()) {
                    val dirEntry = ZipEntry(if (entryName.endsWith("/")) entryName else "$entryName/")
                    zos.putNextEntry(dirEntry)
                    zos.closeEntry()
                } else {
                    children.forEach { child -> addFileToZip(baseDir, child, zos) }
                }
            } else {
                FileInputStream(file).use { fis ->
                    val entry = ZipEntry(entryName)
                    zos.putNextEntry(entry)
                    fis.copyTo(zos, bufferSize = 8 * 1024)
                    zos.closeEntry()
                }
            }
        }
        if (zipFile.exists()) zipFile.delete()
        FileOutputStream(zipFile).use { fos ->
            ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                addFileToZip(sourceDir, sourceDir, zos)
            }
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun normalizeName(name: String): String {
        return name.lowercase()
            .replace("\\s+".toRegex(), "_")
            .replace("[^a-z0-9_]".toRegex(), "")
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    private fun getThemedComponentsFromAssets(context: Context): Set<String> {
        val components = mutableSetOf<String>()
        try {
            val inputStream = context.assets.open("appfilter.xml")
            val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")

            var eventType = parser.eventType
            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component") ?: ""
                    if (component.startsWith("ComponentInfo{")) {
                        val inner = component.removePrefix("ComponentInfo{").removeSuffix("}")
                        if (inner.isNotBlank() && inner.contains("/")) {
                            components.add(inner.lowercase().trim())
                        }
                    }
                }
                eventType = parser.next()
            }
            inputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading appfilter.xml from assets", e)
        }
        return components
    }

    fun getThemedAppCount(context: Context): Int {
        val themedComponents = getThemedComponentsFromAssets(context)
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        var count = 0
        try {
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (ri in resolveInfos) {
                val componentKey = "${ri.activityInfo.packageName}/${ri.activityInfo.name}".lowercase()
                if (themedComponents.contains(componentKey)) {
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error counting themed apps", e)
        }
        return count
    }

    fun getTotalAppCount(context: Context): Int {
        return try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            context.packageManager.queryIntentActivities(mainIntent, 0).size
        } catch (e: Exception) {
            0
        }
    }
}
