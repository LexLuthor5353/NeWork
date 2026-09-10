package ru.netology.nework.core.network.dto

import com.google.gson.annotations.SerializedName

data class AuthDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("token")
    val token: String
)

data class PostCreateDto(
    @SerializedName("content")
    val content: String,
    @SerializedName("coords")
    val coords: CoordsDto? = null,
    @SerializedName("link")
    val link: String? = null,
    @SerializedName("attachment")
    val attachment: AttachmentDto? = null,
    @SerializedName("mentionIds")
    val mentionIds: List<Long> = emptyList()
)

data class UploadResponseDto(
    @SerializedName("url")
    val url: String
)
