package com.akustom15.crush.model

data class WallpaperItem(
    val id: String,
    val name: String,
    val description: String,
    val fileName: String,
    val previewUrl: String? = null
)
