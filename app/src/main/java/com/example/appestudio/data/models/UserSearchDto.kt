package com.example.appestudio.data.models

// Returned from GET /api/users/search
data class UserSearchDto(
    val _id: String,
    val name: String,
    val email: String,
    val career: String,
    val semester: Int
)
