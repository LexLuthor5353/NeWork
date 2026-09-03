package ru.netology.nework.core.network

import ru.netology.nework.core.config.AppSecrets
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ApiKeyInterceptor @Inject constructor(
    private val appSecrets: AppSecrets
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Api-Key", appSecrets.apiKey)
            .build()
        return chain.proceed(request)
    }
}
