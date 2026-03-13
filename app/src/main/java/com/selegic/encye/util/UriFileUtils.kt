package com.selegic.encye.util

import android.content.Context
import android.net.Uri
import java.io.File

fun copyUriToCacheFile(
    context: Context,
    uri: Uri,
    prefix: String
): File? {
    val extension = context.contentResolver.getType(uri)
        ?.substringAfterLast('/', "")
        ?.takeIf { it.isNotBlank() }
        ?: "tmp"
    val tempFile = File.createTempFile(prefix, ".$extension", context.cacheDir)

    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    }.getOrNull()
}
