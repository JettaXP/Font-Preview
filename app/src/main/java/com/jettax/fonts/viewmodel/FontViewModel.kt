package com.jettax.fonts.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jettax.fonts.data.model.FontItem
import com.jettax.fonts.data.repository.FontRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class FontUiState(
    val googleFonts: List<FontItem> = emptyList(),
    val localFonts: List<FontItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val selectedFont: FontItem? = null,
    val isLoading: Boolean = false,
    val loadingFonts: Set<String> = emptySet(),
    val loadedFontFamilies: Map<String, FontFamily> = emptyMap(),
    val loadedFontFiles: Map<String, File> = emptyMap(),
    val previewText: String = "",
    val previewSize: Float = 24f,
    val errorMessage: String? = null
)

class FontViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FontRepository(application)

    private val _uiState = MutableStateFlow(FontUiState())
    val uiState: StateFlow<FontUiState> = _uiState.asStateFlow()

    init {
        loadGoogleFonts()
    }

    private fun loadGoogleFonts() {
        _uiState.value = _uiState.value.copy(
            googleFonts = repository.getGoogleFonts()
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun selectFont(font: FontItem) {
        _uiState.value = _uiState.value.copy(selectedFont = font)
        if (!font.isLocal && !_uiState.value.loadedFontFamilies.containsKey(font.family)) {
            downloadFont(font.family)
        }
    }

    fun updatePreviewText(text: String) {
        _uiState.value = _uiState.value.copy(previewText = text)
    }

    fun updatePreviewSize(size: Float) {
        _uiState.value = _uiState.value.copy(previewSize = size)
    }

    fun downloadFont(family: String) {
        if (_uiState.value.loadedFontFamilies.containsKey(family)) return
        if (_uiState.value.loadingFonts.contains(family)) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loadingFonts = _uiState.value.loadingFonts + family
            )
            try {
                val file = repository.downloadFont(family)
                if (file != null) {
                    val fontFamily = FontFamily(Font(file))
                    _uiState.value = _uiState.value.copy(
                        loadedFontFamilies = _uiState.value.loadedFontFamilies + (family to fontFamily),
                        loadedFontFiles = _uiState.value.loadedFontFiles + (family to file),
                        loadingFonts = _uiState.value.loadingFonts - family
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        loadingFonts = _uiState.value.loadingFonts - family,
                        errorMessage = "Не удалось скачать $family"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loadingFonts = _uiState.value.loadingFonts - family,
                    errorMessage = e.message
                )
            }
        }
    }

    fun loadLocalFont(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = repository.loadLocalFont(uri)
                if (result != null) {
                    val (fontItem, file) = result
                    val fontFamily = FontFamily(Font(file))
                    _uiState.value = _uiState.value.copy(
                        localFonts = _uiState.value.localFonts + fontItem,
                        loadedFontFamilies = _uiState.value.loadedFontFamilies + (fontItem.family to fontFamily),
                        loadedFontFiles = _uiState.value.loadedFontFiles + (fontItem.family to file),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Не удалось загрузить шрифт"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getFilteredGoogleFonts(): List<FontItem> {
        val state = _uiState.value
        return state.googleFonts.filter { font ->
            val matchesSearch = state.searchQuery.isEmpty() ||
                font.family.contains(state.searchQuery, ignoreCase = true)
            val matchesCategory = state.selectedCategory == "All" ||
                font.category.equals(state.selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    fun getFontFile(family: String): File? {
        return _uiState.value.loadedFontFiles[family]
    }
}
