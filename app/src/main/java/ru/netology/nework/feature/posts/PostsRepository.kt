package ru.netology.nework.feature.posts

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import ru.netology.nework.core.model.Post
import ru.netology.nework.core.network.ApiService
import ru.netology.nework.core.network.dto.PostCreateDto
import ru.netology.nework.core.network.toPost
import ru.netology.nework.core.session.TokenStore
import ru.netology.nework.core.util.FilePartUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostsRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenStore: TokenStore,
    @ApplicationContext private val context: Context
) {

    suspend fun loadLatestPosts(): List<Post> {
        val response = apiService.getLatestPosts(50)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код ${response.code()}")
        }
        val body = response.body() ?: return emptyList()
        return body.map { it.toPost() }
    }

    suspend fun createPost(content: String, attachmentUri: Uri? = null, mentionUserIds: List<String> = emptyList()) {
        val token = tokenStore.tokenFlow().first()
        if (token.isNullOrBlank()) {
            throw Exception("сначала войди в аккаунт")
        }
        var attachment: ru.netology.nework.core.network.dto.AttachmentDto? = null
        if (attachmentUri != null) {
            val filePart = FilePartUtils.createPart(context, attachmentUri)
            val uploadResponse = apiService.uploadMedia(filePart)
            if (!uploadResponse.isSuccessful) {
                throw Exception("не удалось загрузить файл, код ${uploadResponse.code()}")
            }
            val uploadBody = uploadResponse.body()
            if (uploadBody == null) {
                throw Exception("пустой ответ при загрузке файла")
            }
            val mimeType = FilePartUtils.getMimeType(context, attachmentUri)
            attachment = ru.netology.nework.core.network.dto.AttachmentDto(
                url = uploadBody.url,
                type = FilePartUtils.getAttachmentType(mimeType)
            )
        }
        val mentionIds = mentionUserIds.mapNotNull { it.toLongOrNull() }
        val response = apiService.createPost(
            PostCreateDto(
                content = content,
                attachment = attachment,
                mentionIds = mentionIds
            )
        )
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код ${response.code()}")
        }
    }

    suspend fun likePost(postId: Long) {
        val response = apiService.likePost(postId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код ${response.code()}")
        }
    }

    suspend fun unlikePost(postId: Long) {
        val response = apiService.unlikePost(postId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код ${response.code()}")
        }
    }

    suspend fun deletePost(postId: Long) {
        val response = apiService.deletePost(postId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код ${response.code()}")
        }
    }
}
