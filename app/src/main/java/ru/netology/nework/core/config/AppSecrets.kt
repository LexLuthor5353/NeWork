package ru.netology.nework.core.config

import ru.netology.nework.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSecrets @Inject constructor() {

    val baseUrl: String
        get() = BuildConfig.BASE_URL

    val apiKey: String
        get() = BuildConfig.API_KEY

    val mapsApiKey: String
        get() = BuildConfig.MAPS_API_KEY
}
