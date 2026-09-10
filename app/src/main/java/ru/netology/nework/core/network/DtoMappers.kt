package ru.netology.nework.core.network

import ru.netology.nework.core.model.Attachment
import ru.netology.nework.core.model.AttachmentType
import ru.netology.nework.core.model.Coordinates
import ru.netology.nework.core.model.Event
import ru.netology.nework.core.model.EventType
import ru.netology.nework.core.model.Job
import ru.netology.nework.core.model.Post
import ru.netology.nework.core.model.User
import ru.netology.nework.core.network.dto.AttachmentDto
import ru.netology.nework.core.network.dto.AttachmentTypeDto
import ru.netology.nework.core.network.dto.CoordsDto
import ru.netology.nework.core.network.dto.EventDto
import ru.netology.nework.core.network.dto.EventTypeDto
import ru.netology.nework.core.network.dto.JobDto
import ru.netology.nework.core.network.dto.PostDto
import ru.netology.nework.core.network.dto.UserDto
import java.time.Instant

fun PostDto.toPost(): Post {
    return Post(
        id = id.toString(),
        authorId = authorId.toString(),
        authorName = author,
        authorAvatarUrl = authorAvatar,
        authorJob = authorJob,
        content = content,
        publishedAt = parseDate(published),
        link = link,
        attachment = attachment?.toAttachment(),
        mentionedUserIds = mentionIds.map { it.toString() },
        likeOwnerIds = likeOwnerIds.map { it.toString() },
        likeOwnerIdsCount = likeOwnerIds.size.toLong(),
        likedByMe = likedByMe
    )
}

fun EventDto.toEvent(): Event {
    val eventType = when (type) {
        EventTypeDto.ONLINE -> EventType.ONLINE
        EventTypeDto.OFFLINE -> EventType.OFFLINE
    }
    return Event(
        id = id.toString(),
        authorId = authorId.toString(),
        authorName = author,
        authorAvatarUrl = authorAvatar,
        authorJob = authorJob,
        content = content,
        publishedAt = parseDate(published),
        eventAt = parseDate(datetime),
        type = eventType,
        link = link,
        attachment = attachment?.toAttachment(),
        participantIds = participantsIds.map { it.toString() },
        speakerIds = speakerIds.map { it.toString() },
        likeOwnerIdsCount = likeOwnerIds.size.toLong(),
        likedByMe = likedByMe
    )
}

fun UserDto.toUser(): User {
    return User(
        id = id.toString(),
        login = login,
        name = name,
        avatarUrl = avatar
    )
}

fun JobDto.toJob(): Job {
    return Job(
        id = id?.toString(),
        company = name ?: "",
        position = position ?: "",
        link = link,
        startAt = parseDate(start),
        finishAt = parseDate(finish)
    )
}

private fun AttachmentDto.toAttachment(): Attachment {
    val attachmentType = when (type) {
        AttachmentTypeDto.IMAGE -> AttachmentType.IMAGE
        AttachmentTypeDto.VIDEO -> AttachmentType.VIDEO
        AttachmentTypeDto.AUDIO -> AttachmentType.AUDIO
    }
    return Attachment(
        type = attachmentType,
        url = url,
        localUri = null,
        sizeBytes = null
    )
}

private fun CoordsDto.toCoordinates(): Coordinates {
    val latitude = lat.toDoubleOrNull() ?: 0.0
    val longitudeValue = longitude.toDoubleOrNull() ?: 0.0
    return Coordinates(latitude, longitudeValue)
}

private fun parseDate(value: String?): Long? {
    if (value == null) {
        return null
    }
    return try {
        Instant.parse(value).toEpochMilli()
    } catch (exception: Exception) {
        null
    }
}
