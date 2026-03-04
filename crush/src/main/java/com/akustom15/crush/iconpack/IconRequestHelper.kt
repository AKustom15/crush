package com.akustom15.crush.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.akustom15.crush.R
import com.akustom15.crush.model.IconItem

/**
 * Helper para enviar solicitudes de iconos (gratis y premium) por email.
 * Lee las apps instaladas en el dispositivo y genera el email con la lista.
 */
object IconRequestHelper {
    private const val TAG = "IconRequestHelper"

    /**
     * Modelo para representar una app instalada que no tiene icono en el pack.
     */
    data class MissingIconApp(
        val appName: String,
        val packageName: String,
        val activityName: String,
        var isSelected: Boolean = false
    )

    /**
     * Obtiene la lista de apps instaladas que NO tienen icono en el pack.
     * Compara las apps del dispositivo con los componentes del appfilter.xml.
     */
    fun getMissingIconApps(context: Context): List<MissingIconApp> {
        val missingApps = mutableListOf<MissingIconApp>()

        try {
            // Obtener los componentes ya mapeados en appfilter.xml
            val mappedComponents = getMappedComponents(context)

            // Obtener todas las apps con launcher intent
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (ri in resolveInfos) {
                val componentKey = "${ri.activityInfo.packageName}/${ri.activityInfo.name}"

                // Si el componente no está mapeado, es un icono faltante
                if (!mappedComponents.contains(componentKey) &&
                    !mappedComponents.contains(ri.activityInfo.packageName)
                ) {
                    val appName = try {
                        ri.loadLabel(pm).toString()
                    } catch (e: Exception) {
                        ri.activityInfo.packageName
                    }

                    missingApps.add(
                        MissingIconApp(
                            appName = appName,
                            packageName = ri.activityInfo.packageName,
                            activityName = ri.activityInfo.name
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo apps sin icono", e)
        }

        return missingApps.sortedBy { it.appName.lowercase() }
    }

    /**
     * Envía la solicitud de iconos gratis por email.
     */
    fun sendFreeRequest(
        context: Context,
        email: String,
        appName: String,
        selectedApps: List<MissingIconApp>
    ) {
        if (selectedApps.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.icon_request_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val subject = context.getString(R.string.icon_request_email_subject, appName)
        val body = buildRequestBody(context, selectedApps, isPremium = false)

        sendEmail(context, email, subject, body)
    }

    /**
     * Envía la solicitud de iconos premium por email.
     */
    fun sendPremiumRequest(
        context: Context,
        email: String,
        appName: String,
        selectedApps: List<MissingIconApp>
    ) {
        if (selectedApps.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.icon_request_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val subject = context.getString(R.string.icon_request_premium_email_subject, appName)
        val body = buildRequestBody(context, selectedApps, isPremium = true)

        sendEmail(context, email, subject, body)
    }

    /**
     * Construye el cuerpo del email con la lista de apps seleccionadas.
     */
    private fun buildRequestBody(
        context: Context,
        selectedApps: List<MissingIconApp>,
        isPremium: Boolean
    ): String {
        val sb = StringBuilder()
        sb.appendLine("Icon Request${if (isPremium) " (Premium)" else ""}")
        sb.appendLine("─────────────────────")
        sb.appendLine()

        // Información del dispositivo
        sb.appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        sb.appendLine("Android: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
        sb.appendLine("App: ${context.packageName}")

        try {
            val pi = context.packageManager.getPackageInfo(context.packageName, 0)
            sb.appendLine("Version: ${pi.versionName}")
        } catch (_: Exception) {}

        sb.appendLine()
        sb.appendLine("Requested Icons (${selectedApps.size}):")
        sb.appendLine("─────────────────────")

        selectedApps.forEach { app ->
            sb.appendLine()
            sb.appendLine("Name: ${app.appName}")
            sb.appendLine("Package: ${app.packageName}")
            sb.appendLine("Activity: ${app.activityName}")
            sb.appendLine("ComponentInfo{${app.packageName}/${app.activityName}}")
        }

        return sb.toString()
    }

    /**
     * Abre el cliente de email con los datos de la solicitud.
     */
    private fun sendEmail(context: Context, email: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.icon_request_no_email), Toast.LENGTH_SHORT).show()
            Log.e(TAG, "No se encontró app de email", e)
        }
    }

    /**
     * Obtiene los componentes mapeados en appfilter.xml.
     */
    private fun getMappedComponents(context: Context): Set<String> {
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
                    val packageAttr = parser.getAttributeValue(null, "package") ?: ""

                    // Extraer package/activity del ComponentInfo{...}
                    if (component.startsWith("ComponentInfo{")) {
                        val inner = component.removePrefix("ComponentInfo{").removeSuffix("}")
                        components.add(inner)
                        // También agregar solo el package name
                        val pkg = inner.split("/").firstOrNull() ?: ""
                        if (pkg.isNotEmpty()) components.add(pkg)
                    }

                    if (packageAttr.isNotEmpty()) {
                        components.add(packageAttr)
                    }
                }
                eventType = parser.next()
            }
            inputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error leyendo appfilter.xml para componentes mapeados", e)
        }
        return components
    }

    /**
     * Cuenta las apps del dispositivo que tienen icono en el pack (themed).
     */
    fun getThemedAppCount(context: Context): Int {
        val mappedComponents = getMappedComponents(context)
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        var count = 0
        try {
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (ri in resolveInfos) {
                val componentKey = "${ri.activityInfo.packageName}/${ri.activityInfo.name}"
                if (mappedComponents.contains(componentKey) ||
                    mappedComponents.contains(ri.activityInfo.packageName)
                ) {
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error contando apps con tema", e)
        }
        return count
    }

    /**
     * Cuenta el total de apps con launcher intent en el dispositivo.
     */
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
