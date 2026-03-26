package com.example.appestudio.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class FeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState

    init { loadPosts() }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            try {
                val response = RetrofitClient.instance.getPosts()
                if (response.isSuccessful) {
                    _uiState.value = FeedUiState.Success(response.body() ?: emptyList())
                } else {
                    _uiState.value = FeedUiState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = FeedUiState.Error("Sin conexión al servidor")
            }
        }
    }
}
