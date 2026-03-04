package com.akustom15.crush.model

/**
 * Modelo que representa un icono individual del icon pack.
 * Contiene la información necesaria para mostrar y aplicar el icono.
 */
data class IconItem(
    val name: String,
    val drawableName: String,
    val resourceId: Int = 0,
    val componentInfo: String = "",
    val category: String = ""
) {
    /** Nombre formateado para mostrar en la UI (reemplaza guiones bajos por espacios) */
    val formattedName: String
        get() = drawableName
            .removePrefix("icon_")
            .removePrefix("ic_")
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
}
