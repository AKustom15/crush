package com.akustom15.crush.model

data class WidgetItem(
    val id: String,
    val name: String,
    val description: String,
    val fileName: String,
    val previewUrl: String? = null,
    val widgetSize: String? = null
)
