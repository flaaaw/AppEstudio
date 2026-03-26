package com.example.appestudio.ui.screens.feed

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appestudio.data.models.PostDto
import com.example.appestudio.data.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val post: PostDto) : UploadState()
    data class Error(val message: String) : UploadState()
}

class UploadViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun uploadPost(context: Context, title: String, content: String, author: String, tags: List<String>, fileUri: Uri?) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            try {
                // Texts to RequestBody
                val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
                val authorBody = author.toRequestBody("text/plain".toMediaTypeOrNull())
                val tagsBody = tags.joinToString(",").toRequestBody("text/plain".toMediaTypeOrNull())

                var filePart: MultipartBody.Part? = null
                
                if (fileUri != null) {
                    val file = getFileFromUri(context, fileUri)
                    if (file != null) {
                        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                        filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    }
                }

                val response = apiService.createPost(
                    title = titleBody,
                    content = contentBody,
                    author = authorBody,
                    tags = tagsBody,
                    file = filePart
                )

                if (response.isSuccessful && response.body() != null) {
                    _uploadState.value = UploadState.Success(response.body()!!)
                } else {
                    _uploadState.value = UploadState.Error("Hubo un error al subir la publicación.")
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload", ".tmp", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return tempFile
    }
}
