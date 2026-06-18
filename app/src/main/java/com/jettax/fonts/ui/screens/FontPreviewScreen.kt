package com.jettax.fonts.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jettax.fonts.viewmodel.FontViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontPreviewScreen(
    fontFamily: String,
    viewModel: FontViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val loadedFont = uiState.loadedFontFamilies[fontFamily]
    val displayFont = loadedFont ?: FontFamily.Default

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(fontFamily) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Размер: ${uiState.previewSize.toInt()} sp",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = uiState.previewSize,
                        onValueChange = { viewModel.updatePreviewSize(it) },
                        valueRange = 8f..72f,
                        steps = 15
                    )
                }
            }
            OutlinedTextField(
                value = uiState.previewText,
                onValueChange = { viewModel.updatePreviewText(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Свой текст") },
                placeholder = { Text("Введите текст для предпросмотра…") },
                shape = MaterialTheme.shapes.large
            )
            if (uiState.previewText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = uiState.previewText,
                        fontFamily = displayFont,
                        fontSize = uiState.previewSize.sp,
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            PreviewSection("Display Large", displayFont, 48.sp, "Аа Бб Вв")
            PreviewSection("Display Medium", displayFont, 36.sp, "Аа Бб Вв Гг")
            PreviewSection("Display Small", displayFont, 30.sp, "Быстрая лиса")

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            PreviewSection("Headline Large", displayFont, 28.sp, "Коричневая лиса прыгает")
            PreviewSection("Headline Medium", displayFont, 24.sp, "The quick brown fox jumps")
            PreviewSection("Headline Small", displayFont, 20.sp, "The quick brown fox jumps over the lazy dog")

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            PreviewSection("Body Large", displayFont, 16.sp, "Съешь же ещё этих мягких французских булок, да выпей чаю. The quick brown fox jumps over the lazy dog.")
            PreviewSection("Body Medium", displayFont, 14.sp, "Съешь же ещё этих мягких французских булок, да выпей чаю. The quick brown fox jumps over the lazy dog.")
            PreviewSection("Body Small", displayFont, 12.sp, "0123456789 АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ абвгдеёжзийклмнопрстуфхцчшщъыьэюя")

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Панграммы",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "The quick brown fox jumps over the lazy dog",
                        fontFamily = displayFont,
                        fontSize = uiState.previewSize.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Съешь же ещё этих мягких французских булок, да выпей чаю",
                        fontFamily = displayFont,
                        fontSize = uiState.previewSize.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "0123456789 !@#\$%^&*()_+-=[]{}",
                        fontFamily = displayFont,
                        fontSize = uiState.previewSize.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Начертания",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val weights = listOf(
                        "Thin" to FontWeight.Thin,
                        "Light" to FontWeight.Light,
                        "Regular" to FontWeight.Normal,
                        "Medium" to FontWeight.Medium,
                        "Bold" to FontWeight.Bold,
                        "Black" to FontWeight.Black,
                    )
                    weights.forEach { (name, weight) ->
                        Text(
                            text = "$name — Быстрая лиса",
                            fontFamily = displayFont,
                            fontWeight = weight,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PreviewSection(
    label: String,
    fontFamily: FontFamily,
    fontSize: androidx.compose.ui.unit.TextUnit,
    sampleText: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = sampleText,
            fontFamily = fontFamily,
            fontSize = fontSize,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
