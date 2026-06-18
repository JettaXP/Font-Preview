package com.jettax.fonts.data.model

data class FontItem(
    val family: String,
    val category: String,
    val variants: List<String> = listOf("regular"),
    val isLocal: Boolean = false,
    val localUri: String? = null,
    val fileName: String? = null
)
