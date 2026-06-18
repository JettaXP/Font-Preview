package com.jettax.fonts.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jettax.fonts.viewmodel.FontViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallFontScreen(viewModel: FontViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val availableFonts = uiState.loadedFontFiles.keys.toList()
    var selectedFont by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Установка на One UI") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Метод #mono_ позволяет установить кастомный шрифт на Samsung устройства с One UI. " +
                            "Шрифт заменяет стандартный Samsung Sans через систему FlipFont.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Работает только на Samsung с One UI. На новых версиях One UI (6+) " +
                            "могут быть ограничения. Используйте на свой риск.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedFont,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Выберите шрифт") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = MaterialTheme.shapes.large
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableFonts.forEach { fontName ->
                        DropdownMenuItem(
                            text = { Text(fontName) },
                            onClick = {
                                selectedFont = fontName
                                expanded = false
                            }
                        )
                    }
                    if (availableFonts.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Сначала скачайте или добавьте шрифт",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { expanded = false }
                        )
                    }
                }
            }
            Button(
                onClick = {
                    val fontFile = uiState.loadedFontFiles[selectedFont]
                    if (fontFile != null) {
                        generateFlipFontPackage(context, selectedFont, fontFile)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedFont.isNotEmpty() && uiState.loadedFontFiles.containsKey(selectedFont)
            ) {
                Icon(Icons.Default.InstallMobile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать FlipFont пакет")
            }
            Text(
                text = "Инструкция по установке",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            StepCard(
                stepNumber = 1,
                icon = Icons.Default.InstallMobile,
                title = "Создайте пакет",
                description = "Выберите шрифт выше и нажмите «Создать FlipFont пакет». " +
                    "Будет создан ZIP-архив с файлами шрифта в нужном формате."
            )

            StepCard(
                stepNumber = 2,
                icon = Icons.Outlined.ContentCopy,
                title = "Скопируйте файлы",
                description = "Распакуйте архив. Скопируйте файл .ttf и переименуйте его в " +
                    "SamsungSans-Regular.ttf. Положите в папку /sdcard/Fonts/ на устройстве."
            )

            StepCard(
                stepNumber = 3,
                icon = Icons.Default.PhoneAndroid,
                title = "Примените через zFont",
                description = "Установите приложение zFont 3 из Galaxy Store или Google Play. " +
                    "Откройте → Local → выберите ваш шрифт → Apply."
            )

            StepCard(
                stepNumber = 4,
                icon = Icons.Default.Settings,
                title = "Настройки Samsung",
                description = "Перейдите в Настройки → Дисплей → Размер и стиль шрифта → " +
                    "Стиль шрифта. Выберите установленный шрифт."
            )

            StepCard(
                stepNumber = 5,
                icon = Icons.Default.CheckCircle,
                title = "Готово!",
                description = "Шрифт будет применён ко всей системе. Для возврата к стандартному " +
                    "шрифту выберите Samsung Sans в настройках."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: Int,
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun generateFlipFontPackage(context: Context, fontName: String, fontFile: File) {
    try {
        val outputDir = File(context.cacheDir, "apks").apply { mkdirs() }
        val zipFile = File(outputDir, "${fontName.replace(" ", "_")}_flipfont.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
            zip.putNextEntry(ZipEntry("SamsungSans-Regular.ttf"))
            fontFile.inputStream().use { it.copyTo(zip) }
            zip.closeEntry()
            val weights = listOf("Bold", "Light", "Medium", "Thin")
            weights.forEach { weight ->
                zip.putNextEntry(ZipEntry("SamsungSans-$weight.ttf"))
                fontFile.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
            zip.putNextEntry(ZipEntry("README.txt"))
            val readme = """
                Font: $fontName
                
                Инструкция по установке на Samsung One UI:
                
                Метод 1 (zFont):
                1. Установите zFont 3 из Galaxy Store
                2. Откройте zFont → Local
                3. Выберите файл SamsungSans-Regular.ttf
                4. Нажмите Apply
                
                Метод 2 (вручную):
                1. Скопируйте файлы в /sdcard/Fonts/
                2. Настройки → Дисплей → Стиль шрифта
                3. Выберите новый шрифт
                
                Для возврата выберите Samsung Sans в настройках.
            """.trimIndent()
            zip.write(readme.toByteArray())
            zip.closeEntry()
        }
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Сохранить FlipFont пакет"))

    } catch (e: Exception) {
        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
