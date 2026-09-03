package ru.netology.nework.feature.users

import ru.netology.nework.core.model.Job
import ru.netology.nework.core.model.Post
import ru.netology.nework.core.model.User
import ru.netology.nework.core.network.ApiService
import ru.netology.nework.core.network.toJob
import ru.netology.nework.core.network.toPost
import ru.netology.nework.core.network.toUser
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
        val body = response.body()
        if (body == null) {
            return emptyList()
        }
        val users = ArrayList<User>()
        for (item in body) {
            users.add(item.toUser())
        }
        return users
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
        val body = response.body()
        if (body == null) {
            return emptyList()
        }
        val posts = ArrayList<Post>()
        for (item in body) {
            posts.add(item.toPost())
        }
        return posts
    }

    suspend fun loadUserJobs(userId: String): List<Job> {
        val response = apiService.getUserJobs(userId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
        val body = response.body()
        if (body == null) {
            return emptyList()
        }
        val jobs = ArrayList<Job>()
        for (item in body) {
            jobs.add(item.toJob())
        }
        return jobs
    }
}
