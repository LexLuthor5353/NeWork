package ru.netology.nework.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    fun login(login: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _success.value = false
            try {
                authRepository.login(login, password)
                _success.value = true
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "не удалось войти"
            }
            _loading.value = false
        }
    }

    fun register(
        login: String,
        password: String,
        name: String,
        avatarUri: android.net.Uri? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _success.value = false
            try {
                authRepository.register(login, password, name, avatarUri)
                _success.value = true
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "не удалось зарегистрироваться"
            }
            _loading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetState() {
        _errorMessage.value = null
        _success.value = false
    }
}
