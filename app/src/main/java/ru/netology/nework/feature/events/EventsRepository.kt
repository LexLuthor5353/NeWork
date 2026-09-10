package ru.netology.nework.feature.events

import ru.netology.nework.core.model.Attachment
import ru.netology.nework.core.model.AttachmentType
import ru.netology.nework.core.model.Event
import ru.netology.nework.core.model.EventType
import ru.netology.nework.core.network.ApiService
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventsRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun loadLatestEvents(): List<Event> {
        val response = apiService.getLatestEvents(50)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код ${response.code()}")
        }
        val body = response.body() ?: return emptyList()
        return body.map { dto ->
            val eventType = when (dto.type) {
                ru.netology.nework.core.network.dto.EventTypeDto.ONLINE -> EventType.ONLINE
                ru.netology.nework.core.network.dto.EventTypeDto.OFFLINE -> EventType.OFFLINE
            }
            Event(
                id = dto.id.toString(),
                authorId = dto.authorId.toString(),
                authorName = dto.author,
                authorAvatarUrl = dto.authorAvatar,
                authorJob = dto.authorJob,
                content = dto.content,
                publishedAt = dto.published.toEpochMillis(),
                eventAt = dto.datetime.toEpochMillis(),
                type = eventType,
                link = dto.link,
                attachment = dto.attachment?.let { attachmentDto ->
                    Attachment(
                        type = when (attachmentDto.type) {
                            ru.netology.nework.core.network.dto.AttachmentTypeDto.IMAGE -> AttachmentType.IMAGE
                            ru.netology.nework.core.network.dto.AttachmentTypeDto.VIDEO -> AttachmentType.VIDEO
                            ru.netology.nework.core.network.dto.AttachmentTypeDto.AUDIO -> AttachmentType.AUDIO
                        },
                        url = attachmentDto.url,
                        localUri = null,
                        sizeBytes = null
                    )
                },
                participantIds = dto.participantsIds.map { it.toString() },
                speakerIds = dto.speakerIds.map { it.toString() },
                likeOwnerIdsCount = dto.likeOwnerIds.size.toLong(),
                likedByMe = dto.likedByMe
            )
        }
    }

    private fun String.toEpochMillis(): Long {
        return try {
            Instant.parse(this).toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun likeEvent(eventId: Long) {
        val response = apiService.likeEvent(eventId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код ${response.code()}")
        }
    }

    suspend fun unlikeEvent(eventId: Long) {
        val response = apiService.unlikeEvent(eventId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код ${response.code()}")
        }
    }

    suspend fun deleteEvent(eventId: Long) {
        val response = apiService.deleteEvent(eventId)
        if (!response.isSuccessful) {
            throw Exception("сервер вернул код ${response.code()}")
        }
    }
}
