package ru.netology.nework.feature.events

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.core.network.ApiService
import ru.netology.nework.core.network.dto.AttachmentDto
import ru.netology.nework.core.network.dto.EventCreateDto
import ru.netology.nework.core.util.FilePartUtils
import javax.inject.Inject

@HiltViewModel
class EditEventViewModel @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    private val MAX_ATTACHMENT_SIZE = 15L * 1024 * 1024

    fun saveEvent(
        content: String,
        attachmentUri: Uri? = null,
        type: String = "ONLINE",
        eventDateMillis: Long? = null,
        speakerUserIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _saved.value = false
            try {
                var attachment: AttachmentDto? = null
                if (attachmentUri != null) {
                    val fileSize = getFileSize(attachmentUri)
                    if (fileSize > MAX_ATTACHMENT_SIZE) {
                        _errorMessage.value = "файл слишком большой, макс 15МБ"
                        _loading.value = false
                        return@launch
                    }
                    val filePart = FilePartUtils.createPart(context, attachmentUri)
                    val uploadResponse = apiService.uploadMedia(filePart)
                    if (!uploadResponse.isSuccessful) {
                        throw Exception("не удалось загрузить файл, код " + uploadResponse.code())
                    }
                    val uploadBody = uploadResponse.body()
                    if (uploadBody == null) {
                        throw Exception("пустой ответ при загрузке файла")
                    }
                    val mimeType = FilePartUtils.getMimeType(context, attachmentUri)
                    attachment = AttachmentDto(
                        url = uploadBody.url,
                        type = FilePartUtils.getAttachmentType(mimeType)
                    )
                }
                val speakerIds = speakerUserIds.mapNotNull { it.toLongOrNull() }
                val datetimeStr = if (eventDateMillis != null) {
                    java.time.Instant.ofEpochMilli(eventDateMillis).toString()
                } else {
                    null
                }
                val response = apiService.createEvent(
                    EventCreateDto(
                        content = content,
                        type = type,
                        datetime = datetimeStr,
                        attachment = attachment,
                        speakerIds = speakerIds
                    )
                )
                if (!response.isSuccessful) {
                    throw Exception("сервер вернул код " + response.code())
                }
                _saved.value = true
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "не удалось сохранить событие"
            }
            _loading.value = false
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.available()?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
