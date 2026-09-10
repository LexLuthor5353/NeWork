package ru.netology.nework.core.model

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class User(
    val id: String?,
    val login: String,
    val name: String?,
    val avatarUrl: String?
) {
    val displayName: String
        get() = name ?: login
}

data class Coordinates(
    val lat: Double,
    val lng: Double
)

data class Attachment(
    val type: AttachmentType,
    val url: String?,
    val localUri: String?,
    val sizeBytes: Long?
)

enum class AttachmentType { IMAGE, VIDEO, AUDIO }

data class Post(
    val id: String?,
    val authorId: String?,
    val authorName: String?,
    val authorAvatarUrl: String?,
    val authorJob: String?,
    val content: String,
    val publishedAt: Long?,
    val link: String?,
    val attachment: Attachment?,
    val mentionedUserIds: List<String>?,
    val likeOwnerIds: List<String>,
    val likeOwnerIdsCount: Long?,
    val likedByMe: Boolean?
) {
    val publishedFormatted: String
        get() = publishedAt?.let {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
            formatter.format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime())
        } ?: ""

    val authorDisplay: User
        get() = User(authorId, "", authorName ?: "без имени", authorAvatarUrl)
}

enum class EventType { ONLINE, OFFLINE }

data class Event(
    val id: String?,
    val authorId: String?,
    val authorName: String?,
    val authorAvatarUrl: String?,
    val authorJob: String?,
    val content: String,
    val publishedAt: Long?,
    val eventAt: Long?,
    val type: EventType,
    val link: String?,
    val attachment: Attachment?,
    val participantIds: List<String>?,
    val speakerIds: List<String>?,
    val likeOwnerIdsCount: Long?,
    val likedByMe: Boolean?
) {
    val publishedFormatted: String
        get() = publishedAt?.let {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
            formatter.format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime())
        } ?: ""

    val eventAtFormatted: String
        get() = eventAt?.let {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
            formatter.format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime())
        } ?: ""

    val typeFormatted: String
        get() = when (type) {
            EventType.ONLINE -> "Online"
            EventType.OFFLINE -> "Offline"
        }

    val authorDisplay: User
        get() = User(authorId, "", authorName ?: "без имени", authorAvatarUrl)
}

data class Job(
    val id: String?,
    val company: String,
    val position: String,
    val link: String?,
    val startAt: Long?,
    val finishAt: Long?
) {
    val periodFormatted: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
            val start = startAt?.let {
                formatter.format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime())
            } ?: ""
            val end = finishAt?.let {
                " — " + formatter.format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime())
            } ?: " — н.в."
            return if (start.isNotEmpty()) "$start$end" else end
        }
}

data class AuthState(
    val isAuthorized: Boolean,
    val token: String?,
    val myUserId: String?
)
