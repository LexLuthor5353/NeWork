package ru.netology.nework.feature.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.core.common.UiState
import ru.netology.nework.core.model.Post
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postsRepository: PostsRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<UiState<List<Post>>>(UiState.Loading)
    val posts: StateFlow<UiState<List<Post>>> = _posts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val loadedPosts = postsRepository.loadLatestPosts()
                _posts.value = UiState.Success(loadedPosts)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "не удалось загрузить посты"
            }
            _loading.value = false
        }
    }

    fun likePost(post: Post) {
        viewModelScope.launch {
            post.id?.toLongOrNull()?.let { postId ->
                try {
                    val currentLiked = post.likedByMe ?: false
                    if (currentLiked) {
                        postsRepository.unlikePost(postId)
                    } else {
                        postsRepository.likePost(postId)
                    }
                    val currentPosts = (_posts.value as? UiState.Success)?.data?.toMutableList() ?: mutableListOf()
                    val index = currentPosts.indexOfFirst { it.id == post.id }
                    if (index >= 0) {
                        val current = currentPosts[index]
                        currentPosts[index] = current.copy(
                            likedByMe = !currentLiked,
                            likeOwnerIdsCount = (current.likeOwnerIdsCount ?: 0) + if (currentLiked) -1 else 1
                        )
                        _posts.value = UiState.Success(currentPosts)
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (exception: Exception) {
                    _errorMessage.value = "не удалось обновить лайк"
                }
            }
        }
    }

    fun likePostById(postId: String) {
        viewModelScope.launch {
            postId.toLongOrNull()?.let { postIdLong ->
                try {
                    postsRepository.likePost(postIdLong)
                    _errorMessage.value = null
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (exception: Exception) {
                    _errorMessage.value = "не удалось поставить лайк"
                }
            }
        }
    }

    fun unlikePostById(postId: String) {
        viewModelScope.launch {
            postId.toLongOrNull()?.let { postIdLong ->
                try {
                    postsRepository.unlikePost(postIdLong)
                    _errorMessage.value = null
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (exception: Exception) {
                    _errorMessage.value = "не удалось убрать лайк"
                }
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            post.id?.toLongOrNull()?.let { postId ->
                try {
                    postsRepository.deletePost(postId)
                    val currentPosts = (_posts.value as? UiState.Success)?.data?.toMutableList() ?: mutableListOf()
                    currentPosts.removeAll { it.id == post.id }
                    _posts.value = UiState.Success(currentPosts)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (exception: Exception) {
                    _errorMessage.value = "не удалось удалить пост"
                }
            }
        }
    }
}
