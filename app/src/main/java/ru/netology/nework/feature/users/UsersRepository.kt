package ru.netology.nework.feature.users

import ru.netology.nework.core.model.Job
import ru.netology.nework.core.model.Post
import ru.netology.nework.core.model.User
import ru.netology.nework.core.network.ApiService
import ru.netology.nework.core.network.dto.JobCreateDto
import ru.netology.nework.core.network.toJob
import ru.netology.nework.core.network.toPost
import ru.netology.nework.core.network.toUser
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun loadUsers(): List<User> {
        val response = apiService.getUsers()
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
        val body = response.body() ?: return emptyList()
        return body.map { it.toUser() }
    }

    suspend fun loadUser(userId: String): User {
        val response = apiService.getUser(userId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
        val body = response.body()
        if (body == null) {
            throw Exception("пользователь не найден")
        }
        return body.toUser()
    }

    suspend fun loadUserWall(userId: String): List<Post> {
        val response = apiService.getUserWall(userId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
        val body = response.body() ?: return emptyList()
        return body.map { it.toPost() }
    }

    suspend fun loadUserJobs(userId: String): List<Job> {
        val response = apiService.getUserJobs(userId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
        val body = response.body() ?: return emptyList()
        return body.map { it.toJob() }
    }

    suspend fun loadMyJobs(): List<Job> {
        val response = apiService.getMyJobs()
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
        val body = response.body() ?: return emptyList()
        return body.map { it.toJob() }
    }

    suspend fun createJob(
        name: String,
        position: String,
        link: String?,
        startAt: String?,
        finishAt: String?
    ) {
        val response = apiService.createJob(
            JobCreateDto(
                name = name,
                position = position,
                link = link,
                start = startAt ?: Instant.now().toString(),
                finish = finishAt
            )
        )
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
    }


    suspend fun deleteJob(jobId: Long) {
        val response = apiService.deleteJob(jobId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
    }
}
