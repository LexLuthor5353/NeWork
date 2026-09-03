package ru.netology.nework.core.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.core.network.dto.AuthDto
import ru.netology.nework.core.network.dto.EventDto
import ru.netology.nework.core.network.dto.JobDto
import ru.netology.nework.core.network.dto.PostCreateDto
import ru.netology.nework.core.network.dto.PostDto
import ru.netology.nework.core.network.dto.UploadResponseDto
import ru.netology.nework.core.network.dto.UserDto

interface ApiService {

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun login(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<AuthDto>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun register(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): Response<AuthDto>

    @Multipart
    @POST("users/registration")
    suspend fun registerWithAvatar(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<AuthDto>

    @Multipart
    @POST("media")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): Response<UploadResponseDto>

    @GET("posts/latest")
    suspend fun getLatestPosts(@Query("count") count: Int): Response<List<PostDto>>

    @POST("posts")
    suspend fun createPost(@Body body: PostCreateDto): Response<PostDto>

    @GET("events/latest")
    suspend fun getLatestEvents(@Query("count") count: Int): Response<List<EventDto>>

    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserDto>

    @GET("{author_id}/wall")
    suspend fun getUserWall(@Path("author_id") authorId: String): Response<List<PostDto>>

    @GET("{user_id}/jobs")
    suspend fun getUserJobs(@Path("user_id") userId: String): Response<List<JobDto>>
}
