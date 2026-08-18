package ru.netology.nework.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow

class InMemoryTokenStore : TokenStore {
    private val tokenState: MutableStateFlow<String?> = MutableStateFlow(null)

    override fun tokenFlow(): Flow<String?> = tokenState

    override suspend fun setToken(token: String?) {
        tokenState.emit(token)
    }
}

