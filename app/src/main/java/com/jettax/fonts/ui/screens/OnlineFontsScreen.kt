package com.jettax.fonts.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jettax.fonts.data.GoogleFontsData
import com.jettax.fonts.data.model.FontItem
import com.jettax.fonts.viewmodel.FontViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineFontsScreen(
    viewModel: FontViewModel,
    onFontClick: (FontItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val filteredFonts = viewModel.getFilteredGoogleFonts()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LargeTopAppBar(
            title = { Text("Font Preview") },
            scrollBehavior = scrollBehavior
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Поиск шрифтов…") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            itemsIndexed(GoogleFontsData.categories) { index, category ->
                val label = when (category) {
                    "All" -> "Все"
                    "sans-serif" -> "Sans Serif"
                    "serif" -> "Serif"
                    "display" -> "Display"
                    "handwriting" -> "Рукописные"
                    "monospace" -> "Monospace"
                    else -> category
                }
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { viewModel.updateCategory(category) },
                    label = { Text(label) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(filteredFonts) { index, font ->
                FontCard(
                    font = font,
                    fontFamily = uiState.loadedFontFamilies[font.family],
                    isLoading = uiState.loadingFonts.contains(font.family),
                    onClick = { onFontClick(font) },
                    onAppear = { viewModel.downloadFont(font.family) }
                )
            }
        }
    }
}

@Composable
fun FontCard(
    font: FontItem,
    fontFamily: FontFamily?,
    isLoading: Boolean,
    onClick: () -> Unit,
    onAppear: () -> Unit
) {
    LaunchedEffect(font.family) {
        onAppear()
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = font.family,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = when (font.category) {
                    "sans-serif" -> "Sans Serif"
                    "serif" -> "Serif"
                    "display" -> "Display"
                    "handwriting" -> "Рукописный"
                    "monospace" -> "Monospace"
                    else -> font.category
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                Text(
                    text = "The quick brown fox jumps over the lazy dog",
                    fontFamily = fontFamily ?: FontFamily.Default,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (fontFamily != null)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }
        }
    }
}
