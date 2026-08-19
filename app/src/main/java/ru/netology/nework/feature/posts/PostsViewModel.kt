package ru.netology.nework.feature.posts

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nework.core.model.Post
import ru.netology.nework.core.model.User

data class PostsUiState(
    val items: List<Post> = emptyList()
)

class PostsViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        PostsUiState(
            items = listOf(
                Post(
                    id = "1",
                    author = User("1", "alex", "Alex", null),
                    publishedAt = System.currentTimeMillis(),
                    content = "Первый тестовый пост",
                    link = null,
                    attachment = null,
                    coords = null,
                    mentionedUserIds = emptyList(),
                    likeOwnerIdsCount = 3,
                    likedByMe = false
                ),
                Post(
                    id = "2",
                    author = User("2", "masha", "Masha", null),
                    publishedAt = System.currentTimeMillis(),
                    content = "Тут потом будет загрузка из api",
                    link = null,
                    attachment = null,
                    coords = null,
                    mentionedUserIds = emptyList(),
                    likeOwnerIdsCount = 8,
                    likedByMe = true
                ),
                Post(
                    id = "3",
                    author = User("3", "igor", "Igor", null),
                    publishedAt = System.currentTimeMillis(),
                    content = "Пока просто делаю экран постов",
                    link = null,
                    attachment = null,
                    coords = null,
                    mentionedUserIds = emptyList(),
                    likeOwnerIdsCount = 1,
                    likedByMe = false
                )
            )
        )
    )
    val state = _state.asStateFlow()
}
