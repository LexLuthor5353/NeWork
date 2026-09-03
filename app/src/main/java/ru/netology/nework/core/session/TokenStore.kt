package ru.netology.nework.core.session

import kotlinx.coroutines.flow.Flow

interface TokenStore {
    fun tokenFlow(): Flow<String?>
    fun userIdFlow(): Flow<String?>
    suspend fun setToken(token: String?)
    suspend fun setUserId(userId: String?)
    suspend fun clear()
}
