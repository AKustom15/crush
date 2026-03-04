package com.akustom15.crush.model

import com.google.gson.annotations.SerializedName

data class CloudWallpaperItem(
    @SerializedName("name")
    val name: String,

    @SerializedName("author")
    val author: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("collections")
    val collections: String = "",

    @SerializedName("downloadable")
    val downloadable: Boolean = true,

    @SerializedName("size")
    val size: Long? = null,

    @SerializedName("dimensions")
    val dimensions: String? = null,

    @SerializedName("copyright")
    val copyright: String = ""
) {
    val id: String get() = url.hashCode().toString()
}
