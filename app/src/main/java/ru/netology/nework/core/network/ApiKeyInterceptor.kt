package ru.netology.nework.core.network

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader(NetworkConfig.apiKeyHeaderName, NetworkConfig.apiKeyValue)
            .build()
        return chain.proceed(request)
    }
}

