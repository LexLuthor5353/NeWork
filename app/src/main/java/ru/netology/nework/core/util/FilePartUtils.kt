package ru.netology.nework.core.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.core.network.dto.AttachmentTypeDto

object FilePartUtils {

    fun createPart(context: Context, uri: Uri, partName: String = "file"): MultipartBody.Part {
        val mimeType = getMimeType(context, uri)
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("не удалось открыть файл")
        val bytes = inputStream.use { it.readBytes() }
        if (bytes.isEmpty()) {
            throw Exception("файл пустой")
        }
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
        val fileName = "file.$extension"
        val body = bytes.toRequestBody(mimeType.toMediaType())
        return MultipartBody.Part.createFormData(partName, fileName, body)
    }

    fun getMimeType(context: Context, uri: Uri): String {
        val fromResolver = context.contentResolver.getType(uri)
        if (fromResolver != null && fromResolver.isNotEmpty()) {
            return fromResolver
        }
        return "application/octet-stream"
    }

    fun getAttachmentType(mimeType: String): AttachmentTypeDto {
        if (mimeType.startsWith("video/")) {
            return AttachmentTypeDto.VIDEO
        }
        if (mimeType.startsWith("audio/")) {
            return AttachmentTypeDto.AUDIO
        }
        return AttachmentTypeDto.IMAGE
    }
}
