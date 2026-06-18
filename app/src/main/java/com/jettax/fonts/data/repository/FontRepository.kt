package com.jettax.fonts.data.repository

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import com.jettax.fonts.data.GoogleFontsData
import com.jettax.fonts.data.model.FontItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

class FontRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val fontCacheDir = File(context.cacheDir, "fonts").apply { mkdirs() }

    fun getGoogleFonts(): List<FontItem> = GoogleFontsData.fonts

    suspend fun downloadFont(family: String): File? = withContext(Dispatchers.IO) {
        val cacheFile = File(fontCacheDir, "${family.replace(" ", "_")}.ttf")
        if (cacheFile.exists() && cacheFile.length() > 0) return@withContext cacheFile
        try {
            val file = downloadFromCss(family, cacheFile)
            if (file != null) return@withContext file
        } catch (_: Exception) { }
        try {
            val file = downloadFromZip(family, cacheFile)
            if (file != null) return@withContext file
        } catch (_: Exception) { }

        null
    }

    private fun downloadFromCss(family: String, cacheFile: File): File? {
        val cssUrl = "https://fonts.googleapis.com/css2?family=${
            family.replace(" ", "+")
        }&display=swap"
        val cssRequest = Request.Builder()
            .url(cssUrl)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/KRT16M) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.105 " +
                    "Mobile Safari/537.36"
            )
            .build()

        val cssResponse = client.newCall(cssRequest).execute()
        if (!cssResponse.isSuccessful) return null
        val css = cssResponse.body?.string() ?: return null
        val urlRegex = Regex("""url\(['\"]?(https://fonts\.gstatic\.com/[^)'\"\s]+)['\"]?\)""")
        val match = urlRegex.find(css) ?: return null
        val fontUrl = match.groupValues[1]
        val fontRequest = Request.Builder().url(fontUrl).build()
        val fontResponse = client.newCall(fontRequest).execute()
        if (!fontResponse.isSuccessful) return null
        val bytes = fontResponse.body?.bytes() ?: return null
        if (bytes.size < 1000) return null

        FileOutputStream(cacheFile).use { it.write(bytes) }
        try {
            Typeface.createFromFile(cacheFile)
        } catch (_: Exception) {
            cacheFile.delete()
            return null
        }

        return cacheFile
    }

    private fun downloadFromZip(family: String, cacheFile: File): File? {
        val zipUrl = "https://fonts.google.com/download?family=${
            family.replace(" ", "+")
        }"

        val request = Request.Builder()
            .url(zipUrl)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return null
        val body = response.body?.byteStream() ?: return null
        ZipInputStream(body).use { zip ->
            var entry = zip.nextEntry
            val entries = mutableListOf<Pair<String, ByteArray>>()
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".ttf", ignoreCase = true)) {
                    val data = zip.readBytes()
                    entries.add(entry.name to data)
                }
                entry = zip.nextEntry
            }
            val bestEntry = entries.firstOrNull {
                it.first.contains("Regular", ignoreCase = true) ||
                    it.first.contains("[", ignoreCase = false).not() &&
                    it.first.contains("Bold", ignoreCase = true).not() &&
                    it.first.contains("Italic", ignoreCase = true).not() &&
                    it.first.contains("Light", ignoreCase = true).not() &&
                    it.first.contains("Thin", ignoreCase = true).not()
            } ?: entries.firstOrNull {
                !it.first.contains("[")
            } ?: entries.firstOrNull()

            if (bestEntry != null && bestEntry.second.size > 1000) {
                FileOutputStream(cacheFile).use { it.write(bestEntry.second) }
                try {
                    Typeface.createFromFile(cacheFile)
                    return cacheFile
                } catch (_: Exception) {
                    cacheFile.delete()
                }
            }
        }
        return null
    }

    fun loadLocalFont(uri: Uri): Pair<FontItem, File>? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(uri) ?: "custom_font.ttf"
            val cacheFile = File(fontCacheDir, "local_$fileName")

            FileOutputStream(cacheFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            Typeface.createFromFile(cacheFile)

            val fontName = fileName
                .removeSuffix(".ttf").removeSuffix(".otf")
                .removeSuffix(".TTF").removeSuffix(".OTF")
                .replace("_", " ").replace("-", " ")

            val fontItem = FontItem(
                family = fontName,
                category = "local",
                isLocal = true,
                localUri = uri.toString(),
                fileName = fileName
            )

            return Pair(fontItem, cacheFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex("_display_name")
                if (nameIndex >= 0) return it.getString(nameIndex)
            }
        }
        return uri.lastPathSegment
    }
}
