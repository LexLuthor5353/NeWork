package ru.netology.nework.core.model

data class User(
    val id: String?,
    val login: String,
    val name: String?,
    val avatarUrl: String?
)

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
    val author: User?,
    val publishedAt: Long?,
    val content: String,
    val link: String?,
    val attachment: Attachment?,
    val coords: Coordinates?,
    val mentionedUserIds: List<String>?,
    val likeOwnerIdsCount: Long?,
    val likedByMe: Boolean?
)

enum class EventType { ONLINE, OFFLINE }

data class Event(
    val id: String?,
    val author: User?,
    val publishedAt: Long?,
    val eventAt: Long?,
    val type: EventType,
    val content: String,
    val link: String?,
    val attachment: Attachment?,
    val coords: Coordinates?,
    val participantIds: List<String>?,
    val speakerIds: List<String>?,
    val likeOwnerIdsCount: Long?,
    val likedByMe: Boolean?
)

data class Job(
    val id: String?,
    val company: String,
    val position: String,
    val link: String?,
    val startAt: Long?,
    val finishAt: Long?
)

data class AuthState(
    val isAuthorized: Boolean,
    val token: String?,
    val myUserId: String?
)

