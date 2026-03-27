package com.example.appestudio.data.network

import com.example.appestudio.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- AUTH ---
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // --- POSTS ---
    @GET("api/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<List<PostDto>>

    @GET("api/posts/user/{userId}")
    suspend fun getPostsByUser(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<PostDto>>

    @Multipart
    @POST("api/posts")
    suspend fun createPost(
        @Part("author")   author:  RequestBody,
        @Part("title")    title:   RequestBody,
        @Part("content")  content: RequestBody,
        @Part("tags")     tags:    RequestBody?,
        @Part file: MultipartBody.Part?
    ): Response<PostDto>

    @POST("api/posts/{id}/like")
    suspend fun likePost(
        @Path("id") postId: String,
        @Body body: Map<String, String>
    ): Response<LikeResponse>

    @HTTP(method = "DELETE", path = "api/posts/{id}", hasBody = true)
    suspend fun deletePost(
        @Path("id") postId: String,
        @Body request: DeletePostRequest
    ): Response<Map<String, Boolean>>

    // --- USERS ---
    @GET("api/users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<UserSearchDto>>

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") userId: String,
        @Body request: UpdateUserRequest
    ): Response<UpdateUserResponse>

    @Multipart
    @PUT("api/users/{id}/avatar")
    suspend fun uploadAvatar(
        @Path("id") userId: String,
        @Part avatar: MultipartBody.Part
    ): Response<Map<String, String>>

    // Comments
    @GET("api/posts/{id}/comments")
    suspend fun getComments(@Path("id") postId: String): Response<List<CommentDto>>

    @POST("api/posts/{id}/comments")
    suspend fun addComment(@Path("id") postId: String, @Body request: CreateCommentRequest): Response<CommentDto>

    // --- CHATS ---
    @GET("api/chats/one/{chatId}")
    suspend fun getChatDetail(@Path("chatId") chatId: String): Response<ChatDto>

    @GET("api/chats/{userId}")
    suspend fun getChats(@Path("userId") userId: String): Response<List<ChatDto>>

    @POST("api/chats")
    suspend fun createChat(@Body request: CreateChatRequest): Response<ChatDto>

    @GET("api/chats/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: String): Response<List<MessageDto>>

    @PUT("api/chats/{chatId}")
    suspend fun updateChat(@Path("chatId") chatId: String, @Body request: UpdateChatRequest): Response<ChatDto>

    @POST("api/chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body request: SendMessageRequest
    ): Response<MessageDto>

    @Multipart
    @POST("api/chats/{chatId}/messages/media")
    suspend fun sendMediaMessage(
        @Path("chatId") chatId: String,
        @Part("senderId")   senderId:   RequestBody,
        @Part("senderName") senderName: RequestBody,
        @Part("text")       text:       RequestBody?,
        @Part("mediaType")  mediaType:  RequestBody?,
        @Part file: MultipartBody.Part
    ): Response<MessageDto>

    // --- VIDEOS ---
    @GET("api/videos")
    suspend fun getVideos(@Query("topic") topic: String? = null): Response<List<VideoDto>>

    @POST("api/videos/{id}/view")
    suspend fun incrementView(@Path("id") videoId: String): Response<Map<String, Boolean>>
}
