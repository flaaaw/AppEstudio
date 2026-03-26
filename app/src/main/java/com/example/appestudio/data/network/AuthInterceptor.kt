package com.example.appestudio.data.network

import com.example.appestudio.data.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds the Bearer token to every request.
 * If the server responds with 401 (expired/invalid token), clears the session
 * so MainActivity redirects to the login screen on next launch.
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.getToken()

        val requestBuilder = chain.request().newBuilder()
        if (token.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // If server says token is invalid/expired, clear the local session
        if (response.code == 401) {
            sessionManager.clearSession()
        }

        return response
    }
}
