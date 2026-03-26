package com.example.appestudio.ui.screens.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appestudio.data.local.AppDatabase
import com.example.appestudio.data.local.toDto
import com.example.appestudio.data.local.toEntity
import com.example.appestudio.data.models.PostDto
import com.example.appestudio.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val posts: List<PostDto>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val postDao = db.postDao()

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState

    private var currentPage = 1
    private var isEndReached = false
    private var isCurrentlyLoading = false

    init { 
        loadFromCache()
        loadPosts() 
    }

    private fun loadFromCache() {
        viewModelScope.launch {
            val cached = postDao.getAllPosts().map { it.toDto() }
            if (cached.isNotEmpty()) {
                _uiState.value = FeedUiState.Success(cached)
            }
        }
    }

    fun loadPosts(isRefresh: Boolean = true) {
        if (isCurrentlyLoading) return
        viewModelScope.launch {
            isCurrentlyLoading = true
            if (isRefresh && _uiState.value is FeedUiState.Loading) {
                _uiState.value = FeedUiState.Loading
            }
            if (isRefresh) {
                currentPage = 1
                isEndReached = false
            }
            
            try {
                val response = RetrofitClient.instance.getPosts(page = currentPage, limit = 10)
                if (response.isSuccessful) {
                    val newPosts = response.body() ?: emptyList()
                    if (newPosts.isEmpty()) {
                        isEndReached = true
                    }
                    
                    val currentList = if (isRefresh) emptyList() else {
                        (uiState.value as? FeedUiState.Success)?.posts ?: emptyList()
                    }
                    val updatedList = currentList + newPosts
                    _uiState.value = FeedUiState.Success(updatedList)

                    // Cache first page
                    if (isRefresh && newPosts.isNotEmpty()) {
                        postDao.clearAll()
                        postDao.insertPosts(newPosts.map { it.toEntity() })
                    }
                    
                    currentPage++
                } else {
                    if (isRefresh && _uiState.value is FeedUiState.Loading) {
                        _uiState.value = FeedUiState.Error("Error ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                if (isRefresh && _uiState.value is FeedUiState.Loading) {
                    _uiState.value = FeedUiState.Error("Sin conexión al servidor")
                }
            }
            isCurrentlyLoading = false
        }
    }

    fun loadMorePosts() {
        if (!isEndReached && !isCurrentlyLoading) {
            loadPosts(isRefresh = false)
        }
    }
}
