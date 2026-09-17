package ru.netology.nework.feature.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.core.model.Job
import ru.netology.nework.core.model.Post
import ru.netology.nework.core.model.User
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val loadedUsers = usersRepository.loadUsers()
                _users.value = loadedUsers
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "не удалось загрузить пользователей"
            }
            _loading.value = false
        }
    }

    suspend fun loadUser(userId: String): User {
        return usersRepository.loadUser(userId)
    }

    suspend fun loadUserWall(userId: String): List<Post> {
        return usersRepository.loadUserWall(userId)
    }

    suspend fun loadUserJobs(userId: String): List<Job> {
        return usersRepository.loadUserJobs(userId)
    }

    suspend fun loadMyJobs(): List<Job> {
        return usersRepository.loadMyJobs()
    }

    suspend fun deleteJob(jobId: Long) {
        usersRepository.deleteJob(jobId)
    }
}
