package ru.netology.nework.core.session

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsTokenStore @Inject constructor(
    @ApplicationContext context: Context
) : TokenStore {

    private val preferences = context.getSharedPreferences("nework_auth", Context.MODE_PRIVATE)
    private val tokenState = MutableStateFlow(preferences.getString("token", null))
    private val userIdState = MutableStateFlow(preferences.getString("user_id", null))

    override fun tokenFlow(): Flow<String?> = tokenState

    override fun userIdFlow(): Flow<String?> = userIdState

    override suspend fun setToken(token: String?) {
        preferences.edit().putString("token", token).apply()
        tokenState.emit(token)
    }

    override suspend fun setUserId(userId: String?) {
        preferences.edit().putString("user_id", userId).apply()
        userIdState.emit(userId)
    }

    override suspend fun clear() {
        preferences.edit().clear().apply()
        tokenState.emit(null)
        userIdState.emit(null)
    }
}
