package com.akustom15.crush.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

object PreviewExtractor {

    private const val TAG = "PreviewExtractor"

    private val PORTRAIT_THUMBS = listOf("preset_thumb_portrait.png", "preset_thumb_portrait.jpg")
    private val LANDSCAPE_THUMBS = listOf("preset_thumb_landscape.png", "preset_thumb_landscape.jpg")

    fun extractWidgetPreview(
        context: Context,
        widgetFileName: String,
        usePortrait: Boolean = true
    ): String? {
        return extractPreview(
            context = context,
            assetPath = "widgets/$widgetFileName",
            fileName = widgetFileName,
            usePortrait = usePortrait
        )
    }

    fun extractWallpaperPreview(
        context: Context,
        wallpaperFileName: String,
        usePortrait: Boolean = true
    ): String? {
        return extractPreview(
            context = context,
            assetPath = "wallpapers/$wallpaperFileName",
            fileName = wallpaperFileName,
            usePortrait = usePortrait
        )
    }

    private fun extractPreview(
        context: Context,
        assetPath: String,
        fileName: String,
        usePortrait: Boolean
    ): String? {
        try {
            val previewCacheDir = File(context.filesDir, "previews")
            if (!previewCacheDir.exists()) {
                previewCacheDir.mkdirs()
            }

            val orientation = if (usePortrait) "portrait" else "landscape"
            val outputFileName = "${fileName}_${orientation}.png"
            val outputFile = File(previewCacheDir, outputFileName)

            if (outputFile.exists()) {
                return outputFile.absolutePath
            }

            val inputStream = context.assets.open(assetPath)
            val zipInputStream = ZipInputStream(inputStream)

            val thumbNames = if (usePortrait) PORTRAIT_THUMBS else LANDSCAPE_THUMBS

            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (entry.name in thumbNames) {
                    val bitmap = BitmapFactory.decodeStream(zipInputStream)
                    if (bitmap != null) {
                        FileOutputStream(outputFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        zipInputStream.close()
                        return outputFile.absolutePath
                    }
                }
                entry = zipInputStream.nextEntry
            }

            zipInputStream.close()
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting preview from $fileName", e)
            return null
        }
    }

    fun extractWidgetDescription(context: Context, widgetFileName: String): String? {
        return extractDescription(context, "widgets/$widgetFileName")
    }

    fun extractWallpaperDescription(context: Context, wallpaperFileName: String): String? {
        return extractDescription(context, "wallpapers/$wallpaperFileName")
    }

    private fun extractDescription(context: Context, assetPath: String): String? {
        try {
            val inputStream = context.assets.open(assetPath)
            val zipInputStream = ZipInputStream(inputStream)

            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (entry.name == "preset.json") {
                    val jsonString = zipInputStream.bufferedReader().readText()
                    zipInputStream.close()
                    val json = JSONObject(jsonString)
                    if (json.has("preset_info")) {
                        val info = json.getJSONObject("preset_info")
                        if (info.has("description")) {
                            val desc = info.getString("description")
                            if (desc.isNotBlank()) return desc
                        }
                    }
                    if (json.has("description")) {
                        val desc = json.getString("description")
                        if (desc.isNotBlank()) return desc
                    }
                    return null
                }
                entry = zipInputStream.nextEntry
            }
            zipInputStream.close()
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting description from $assetPath", e)
            return null
        }
    }

    fun clearPreviewCache(context: Context) {
        try {
            val previewFilesDir = File(context.filesDir, "previews")
            if (previewFilesDir.exists()) {
                previewFilesDir.deleteRecursively()
            }
            val previewCacheDir = File(context.cacheDir, "previews")
            if (previewCacheDir.exists()) {
                previewCacheDir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing preview cache", e)
        }
    }
}
