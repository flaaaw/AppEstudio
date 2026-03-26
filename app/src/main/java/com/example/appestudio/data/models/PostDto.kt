package com.example.appestudio.data.models

data class PostDto(
    val _id: String,
    val author: String,
    val authorId: String,
    val timeInfo: String,
    val title: String,
    val content: String,
    val mediaUrl: String?,
    val likes: Int,
    val likedBy: List<String>,
    val comments: Int,
    val isVerified: Boolean,
    val tags: List<String>,
    val createdAt: String
)

data class LikeResponse(
    val likes: Int,
    val liked: Boolean
)

data class DeletePostRequest(
    val userId: String
)

data class UpdateUserRequest(
    val name: String,
    val career: String,
    val semester: Int
)

data class UpdateUserResponse(
    val _id: String,
    val name: String,
    val email: String,
    val career: String,
    val semester: Int
)

data class VideoDto(
    val _id: String,
    val title: String,
    val description: String,
    val topic: String,
    val videoUrl: String,
    val thumbnailUrl: String?,
    val duration: String,
    val uploaderName: String,
    val uploaderId: String,
    val views: Int,
    val createdAt: String
)
