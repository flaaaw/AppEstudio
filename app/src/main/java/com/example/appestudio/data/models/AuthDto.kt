package com.example.appestudio.data.models

// Request bodies
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String, val career: String = "")

// Response
data class AuthUserDto(val id: String, val name: String, val email: String, val career: String, val semester: Int = 1)
data class AuthResponse(val token: String, val user: AuthUserDto)

// Validation error map from the server
data class AuthErrorResponse(val errors: Map<String, String>? = null, val error: String? = null)
