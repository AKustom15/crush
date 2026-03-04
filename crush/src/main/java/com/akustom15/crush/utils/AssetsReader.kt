package com.akustom15.crush.utils

import android.content.Context
import com.akustom15.crush.model.WallpaperItem
import com.akustom15.crush.model.WidgetItem

object AssetsReader {

    private const val WIDGETS_FOLDER = "widgets"
    private const val WALLPAPERS_FOLDER = "wallpapers"
    private const val KWGT_EXTENSION = ".kwgt"
    private const val KLWP_EXTENSION = ".klwp"

    fun getWidgetsFromAssets(context: Context): List<WidgetItem> {
        return try {
            val assetManager = context.assets
            val widgetFiles = assetManager.list(WIDGETS_FOLDER) ?: emptyArray()

            widgetFiles.filter { it.endsWith(KWGT_EXTENSION, ignoreCase = true) }.mapIndexed { index, fileName ->
                val nameWithoutExtension = fileName.removeSuffix(KWGT_EXTENSION)

                val previewPath = PreviewExtractor.extractWidgetPreview(
                    context = context,
                    widgetFileName = fileName,
                    usePortrait = true
                )

                val description = PreviewExtractor.extractWidgetDescription(context, fileName)
                    ?: formatName(nameWithoutExtension)

                WidgetItem(
                    id = "widget_$index",
                    name = formatName(nameWithoutExtension),
                    description = description,
                    fileName = fileName,
                    previewUrl = previewPath
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getWallpapersFromAssets(context: Context): List<WallpaperItem> {
        return try {
            val assetManager = context.assets
            val wallpaperFiles = assetManager.list(WALLPAPERS_FOLDER) ?: emptyArray()

            wallpaperFiles.filter { it.endsWith(KLWP_EXTENSION, ignoreCase = true) }.mapIndexed { index, fileName ->
                val nameWithoutExtension = fileName.removeSuffix(KLWP_EXTENSION)

                val previewPath = PreviewExtractor.extractWallpaperPreview(
                    context = context,
                    wallpaperFileName = fileName,
                    usePortrait = true
                )

                val description = PreviewExtractor.extractWallpaperDescription(context, fileName)
                    ?: formatName(nameWithoutExtension)

                WallpaperItem(
                    id = "wallpaper_$index",
                    name = formatName(nameWithoutExtension),
                    description = description,
                    fileName = fileName,
                    previewUrl = previewPath
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun formatName(fileName: String): String {
        return fileName.replace("_", " ").split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    fun getWidgetAssetPath(fileName: String): String {
        return "$WIDGETS_FOLDER/$fileName"
    }

    fun getWallpaperAssetPath(fileName: String): String {
        return "$WALLPAPERS_FOLDER/$fileName"
    }
}
