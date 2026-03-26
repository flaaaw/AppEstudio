package com.example.appestudio.data

import android.content.Context
import android.content.SharedPreferences

/**
 * SessionManager persists the JWT token and basic user info
 * in SharedPreferences so the session survives app restarts.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME    = "AppEstudioSession"
        private const val KEY_TOKEN     = "jwt_token"
        private const val KEY_NAME      = "user_name"
        private const val KEY_EMAIL     = "user_email"
        private const val KEY_CAREER    = "user_career"
        private const val KEY_USER_ID   = "user_id"
        private const val KEY_SEMESTER  = "user_semester"
        private const val KEY_AVATAR_URL = "user_avatar_url"
    }

    /** Call after a successful login or register */
    fun saveSession(token: String, name: String, email: String, career: String, id: String, semester: Int = 1) {
        prefs.edit()
            .putString(KEY_TOKEN,    token)
            .putString(KEY_NAME,     name)
            .putString(KEY_EMAIL,    email)
            .putString(KEY_CAREER,   career)
            .putString(KEY_USER_ID,  id)
            .putInt(KEY_SEMESTER,    semester)
            .apply()
    }

    /** Update profile fields locally after editing */
    fun updateProfile(name: String, career: String, semester: Int) {
        prefs.edit()
            .putString(KEY_NAME,    name)
            .putString(KEY_CAREER,  career)
            .putInt(KEY_SEMESTER,   semester)
            .apply()
    }

    /** Check if a session already exists */
    fun isLoggedIn(): Boolean = prefs.getString(KEY_TOKEN, null) != null

    fun getToken():    String = prefs.getString(KEY_TOKEN,   "") ?: ""
    fun getName():     String = prefs.getString(KEY_NAME,    "Usuario") ?: "Usuario"
    fun getEmail():    String = prefs.getString(KEY_EMAIL,   "") ?: ""
    fun getCareer():   String = prefs.getString(KEY_CAREER,  "") ?: ""
    fun getUserId():   String = prefs.getString(KEY_USER_ID, "") ?: ""
    fun getSemester(): Int    = prefs.getInt(KEY_SEMESTER, 1)
    fun getAvatarUrl(): String = prefs.getString(KEY_AVATAR_URL, "") ?: ""

    /** Persist avatar URL after upload */
    fun saveAvatarUrl(url: String) {
        prefs.edit().putString(KEY_AVATAR_URL, url).apply()
    }

    /** Call on logout */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
