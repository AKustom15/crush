package com.akustom15.crush.iconpack

import android.content.Context
import android.util.Log
import com.akustom15.crush.model.IconItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Parser del archivo appfilter.xml ubicado en assets/.
 * Lee los componentes mapeados y genera la lista de iconos disponibles.
 */
object AppFilterParser {
    private const val TAG = "AppFilterParser"

    /**
     * Parsea el archivo appfilter.xml desde los assets y devuelve la lista de IconItem.
     * Cada item tiene el drawable name, component info y resource ID resuelto.
     */
    fun parseAppFilter(context: Context): List<IconItem> {
        val icons = mutableListOf<IconItem>()
        val seenDrawables = mutableSetOf<String>()

        try {
            val inputStream = context.assets.open("appfilter.xml")
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component") ?: ""
                    val drawable = parser.getAttributeValue(null, "drawable") ?: ""

                    // Ignorar items vacíos, de máscara/fondo y duplicados
                    if (drawable.isNotEmpty()
                        && drawable != "icon_mask"
                        && drawable != "icon_back"
                        && drawable != "icon_upon"
                        && !seenDrawables.contains(drawable)
                    ) {
                        seenDrawables.add(drawable)
                        val resourceId = getDrawableResourceId(context, drawable)
                        if (resourceId != 0) {
                            icons.add(
                                IconItem(
                                    name = drawable,
                                    drawableName = drawable,
                                    resourceId = resourceId,
                                    componentInfo = component
                                )
                            )
                        }
                    }
                }
                eventType = parser.next()
            }
            inputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando appfilter.xml", e)
        }

        return icons
    }

    /**
     * Parsea el archivo drawable.xml desde assets para obtener todos los iconos
     * organizados por categoría.
     */
    fun parseDrawableXml(context: Context): List<IconItem> {
        val icons = mutableListOf<IconItem>()
        var currentCategory = ""

        try {
            val inputStream = context.assets.open("drawable.xml")
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "category" -> {
                            currentCategory = parser.getAttributeValue(null, "title") ?: ""
                        }
                        "item" -> {
                            val drawable = parser.getAttributeValue(null, "drawable") ?: ""
                            if (drawable.isNotEmpty()) {
                                val resourceId = getDrawableResourceId(context, drawable)
                                if (resourceId != 0) {
                                    icons.add(
                                        IconItem(
                                            name = drawable,
                                            drawableName = drawable,
                                            resourceId = resourceId,
                                            category = currentCategory
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
            inputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando drawable.xml, intentando appfilter.xml", e)
            // Fallback: si no hay drawable.xml, usar appfilter.xml
            if (icons.isEmpty()) {
                return parseAppFilter(context)
            }
        }

        return icons
    }

    /**
     * Cuenta el número total de iconos únicos disponibles en el pack.
     */
    fun getIconCount(context: Context): Int {
        return try {
            val icons = parseDrawableXml(context)
            if (icons.isNotEmpty()) icons.size else parseAppFilter(context).size
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Obtiene la lista de categorías únicas del icon pack.
     */
    fun getCategories(context: Context): List<String> {
        return parseDrawableXml(context)
            .map { it.category }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    /**
     * Resuelve el resource ID de un drawable por su nombre.
     */
    private fun getDrawableResourceId(context: Context, drawableName: String): Int {
        return try {
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        } catch (e: Exception) {
            0
        }
    }
}
