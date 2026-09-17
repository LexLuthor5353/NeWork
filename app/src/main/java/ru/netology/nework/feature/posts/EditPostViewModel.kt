package ru.netology.nework.feature.posts

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.core.network.ApiService
import ru.netology.nework.core.network.dto.AttachmentDto
import ru.netology.nework.core.network.dto.CoordsDto
import ru.netology.nework.core.network.dto.PostCreateDto
import ru.netology.nework.core.util.FilePartUtils
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    private val maxAttachmentSize = 15L * 1024 * 1024

    fun savePost(
        postId: String?,
        content: String,
        lat: Double?,
        lng: Double?,
        attachmentUri: Uri? = null,
        mentionUserIds: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _saved.value = false
            try {
                var attachment: AttachmentDto? = null
                if (attachmentUri != null) {
                    val fileSize = getFileSize(attachmentUri)
                    if (fileSize > maxAttachmentSize) {
                        _errorMessage.value = "файл слишком большой, макс 15 МБ"
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

                val mentionIds = mentionUserIds.mapNotNull { it.toLongOrNull() }

                val coords = if (lat != null && lng != null) {
                    CoordsDto(lat = lat.toString(), longitude = lng.toString())
                } else {
                    null
                }

                val body = PostCreateDto(
                    content = content,
                    coords = coords,
                    attachment = attachment,
                    mentionIds = mentionIds
                )

                _saved.value = true
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "не удалось сохранить пост"
            }
            _loading.value = false
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
        } catch (exception: Exception) {
            0L
        }
    }
}
