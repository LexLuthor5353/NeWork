package ru.netology.nework.feature.auth

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.core.network.ApiService
import ru.netology.nework.core.session.TokenStore
import ru.netology.nework.core.util.FilePartUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenStore: TokenStore,
    @ApplicationContext private val context: Context
) {

    suspend fun login(login: String, password: String) {
        val response = apiService.login(login, password)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
        val body = response.body()
        if (body == null) {
            throw Exception("пустой ответ сервера")
        }
        tokenStore.setToken(body.token)
        tokenStore.setUserId(body.id.toString())
    }

    suspend fun register(login: String, password: String, name: String, avatarUri: Uri? = null) {
        val response = if (avatarUri != null) {
            val filePart = FilePartUtils.createPart(context, avatarUri)
            val loginBody = login.toRequestBody("text/plain".toMediaType())
            val passBody = password.toRequestBody("text/plain".toMediaType())
            val nameBody = name.toRequestBody("text/plain".toMediaType())
            apiService.registerWithAvatar(loginBody, passBody, nameBody, filePart)
        } else {
            apiService.register(login, password, name)
        }
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
        val body = response.body()
        if (body == null) {
            throw Exception("пустой ответ сервера")
        }
        tokenStore.setToken(body.token)
        tokenStore.setUserId(body.id.toString())
    }

    suspend fun logout() {
        tokenStore.clear()
    }
}
