package ru.netology.nework.feature.events

import ru.netology.nework.core.model.Event
import ru.netology.nework.core.network.ApiService
import ru.netology.nework.core.network.toEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventsRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun loadLatestEvents(): List<Event> {
        val response = apiService.getLatestEvents(50)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код " + response.code())
        }
        val body = response.body()
        if (body == null) {
            return emptyList()
        }
        val events = ArrayList<Event>()
        for (item in body) {
            events.add(item.toEvent())
        }
        return events
    }
}
