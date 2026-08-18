package ru.netology.nework.core.session

import kotlinx.coroutines.flow.Flow

interface TokenStore {
    fun tokenFlow(): Flow<String?>
    suspend fun setToken(token: String?)
}

