package ru.netology.nework.feature.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val postsRepository: PostsRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    fun savePost(content: String, attachmentUri: android.net.Uri? = null) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _saved.value = false
            try {
                postsRepository.createPost(content, attachmentUri)
                _saved.value = true
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "не удалось сохранить пост"
            }
            _loading.value = false
        }
    }
}
