package ru.netology.nework.core.network.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("login")
    val login: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar")
    val avatar: String?
)

data class CoordsDto(
    @SerializedName("lat")
    val lat: String,
    @SerializedName("long")
    val longitude: String
)

enum class AttachmentTypeDto {
    @SerializedName("IMAGE")
    IMAGE,
    @SerializedName("VIDEO")
    VIDEO,
    @SerializedName("AUDIO")
    AUDIO
}

data class AttachmentDto(
    @SerializedName("url")
    val url: String,
    @SerializedName("type")
    val type: AttachmentTypeDto
)

data class PostDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("authorId")
    val authorId: Int,
    @SerializedName("author")
    val author: String,
    @SerializedName("authorAvatar")
    val authorAvatar: String?,
    @SerializedName("authorJob")
    val authorJob: String?,
    @SerializedName("content")
    val content: String,
    @SerializedName("published")
    val published: String,
    @SerializedName("coords")
    val coords: CoordsDto?,
    @SerializedName("link")
    val link: String?,
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Int>,
    @SerializedName("mentionIds")
    val mentionIds: List<Int>,
    @SerializedName("likedByMe")
    val likedByMe: Boolean,
    @SerializedName("attachment")
    val attachment: AttachmentDto?
)

enum class EventTypeDto {
    @SerializedName("ONLINE")
    ONLINE,
    @SerializedName("OFFLINE")
    OFFLINE
}

data class EventDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("authorId")
    val authorId: Int,
    @SerializedName("author")
    val author: String,
    @SerializedName("authorAvatar")
    val authorAvatar: String?,
    @SerializedName("authorJob")
    val authorJob: String?,
    @SerializedName("content")
    val content: String,
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("published")
    val published: String,
    @SerializedName("coords")
    val coords: CoordsDto?,
    @SerializedName("type")
    val type: EventTypeDto,
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Int>,
    @SerializedName("likedByMe")
    val likedByMe: Boolean,
    @SerializedName("speakerIds")
    val speakerIds: List<Int>,
    @SerializedName("participantsIds")
    val participantsIds: List<Int>,
    @SerializedName("attachment")
    val attachment: AttachmentDto?,
    @SerializedName("link")
    val link: String?
)

data class JobDto(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("position")
    val position: String?,
    @SerializedName("link")
    val link: String?,
    @SerializedName("start")
    val start: String?,
    @SerializedName("finish")
    val finish: String?
)

data class EventCreateDto(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("content")
    val content: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("datetime")
    val datetime: String? = null,
    @SerializedName("coords")
    val coords: CoordsDto? = null,
    @SerializedName("link")
    val link: String? = null,
    @SerializedName("attachment")
    val attachment: AttachmentDto? = null,
    @SerializedName("speakerIds")
    val speakerIds: List<Long> = emptyList()
)

data class JobCreateDto(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("name")
    val name: String,
    @SerializedName("position")
    val position: String,
    @SerializedName("link")
    val link: String? = null,
    @SerializedName("start")
    val start: String,
    @SerializedName("finish")
    val finish: String? = null
)
