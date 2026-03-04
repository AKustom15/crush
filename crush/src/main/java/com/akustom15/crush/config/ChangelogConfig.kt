package com.akustom15.crush.config

data class ChangelogConfig(
    val entries: List<ChangelogEntry> = emptyList()
)

data class ChangelogEntry(
    val text: String = "",
    val resourceId: Int = 0
) {
    companion object {
        fun fromResource(resId: Int) = ChangelogEntry(resourceId = resId)
        fun fromText(text: String) = ChangelogEntry(text = text)
    }
}
