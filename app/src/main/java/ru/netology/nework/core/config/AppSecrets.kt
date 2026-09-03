package ru.netology.nework.core.config

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSecrets @Inject constructor(
    @ApplicationContext context: Context
) {

    private val properties = Properties()

    init {
        val inputStream = context.assets.open("secrets.properties")
        inputStream.use { stream ->
            properties.load(stream)
        }
    }

    val baseUrl: String
        get() = properties.getProperty("BASE_URL")?.trim() ?: ""

    val apiKey: String
        get() = properties.getProperty("API_KEY")?.trim() ?: ""

    val mapsApiKey: String
        get() = properties.getProperty("MAPS_API_KEY")?.trim() ?: ""

}
