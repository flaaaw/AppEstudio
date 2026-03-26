package com.example.appestudio.data.models

data class CommentDto(
    val _id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String? = "",
    val content: String,
    val createdAt: String
)

data class CreateCommentRequest(
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val content: String
)
