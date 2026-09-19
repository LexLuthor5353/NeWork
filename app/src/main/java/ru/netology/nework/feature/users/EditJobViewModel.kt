package ru.netology.nework.feature.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.core.session.TokenStore
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class EditJobViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    fun saveJob(
        jobId: String?,
        company: String,
        position: String,
        link: String?,
        startAt: Long?,
        finishAt: Long?
    ) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _saved.value = false
            try {
                val start = if (startAt != null) {
                    Instant.ofEpochMilli(startAt).toString()
                } else {
                    Instant.now().toString()
                }
                val finish = if (finishAt != null) {
                    Instant.ofEpochMilli(finishAt).toString()
                } else {
                    null
                }

                usersRepository.createJob(
                    id = jobId?.toLongOrNull(),
                    name = company,
                    position = position,
                    link = link,
                    startAt = start,
                    finishAt = finish
                )
                _saved.value = true
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "не удалось сохранить"
            }
            _loading.value = false
        }
    }
}
