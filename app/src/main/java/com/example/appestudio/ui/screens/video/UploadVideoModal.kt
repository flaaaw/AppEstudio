package com.example.appestudio.ui.screens.video

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.ui.theme.Emerald500
import com.example.appestudio.ui.theme.Red500
import com.example.appestudio.ui.theme.Slate400
import com.example.appestudio.ui.theme.Slate500
import com.example.appestudio.ui.theme.Slate700
import com.example.appestudio.ui.theme.Slate800
import com.example.appestudio.ui.theme.Slate900
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

private val VIDEO_TOPICS = listOf("Programación", "Matemáticas", "Química", "Física", "General")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadVideoModal(
    onDismiss: () -> Unit,
    onUploaded: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("General") }
    var duration by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedUri = it
            selectedName = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(idx)
            } ?: "video.mp4"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Subir Video", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Slate400)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            errorMsg?.let { Text(it, color = Red500, fontSize = 13.sp) }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Título", color = Slate500) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Descripción", color = Slate500) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                placeholder = { Text("Tema", color = Slate500) },
                supportingText = { Text("Sugeridos: ${VIDEO_TOPICS.joinToString()}", color = Slate500, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                placeholder = { Text("Duración (ej. 12:45)", color = Slate500) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { picker.launch("video/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (selectedUri == null) Icons.Default.Upload else Icons.Default.CheckCircle,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(selectedName ?: "Seleccionar video", color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    val uri = selectedUri
                    if (title.isBlank() || uri == null) {
                        errorMsg = "Completa título y selecciona un video."
                        return@Button
                    }
                    scope.launch {
                        isUploading = true
                        errorMsg = null
                        try {
                            val mime = context.contentResolver.getType(uri) ?: "video/mp4"
                            val stream = context.contentResolver.openInputStream(uri)!!
                            val file = File(context.cacheDir, selectedName ?: "video_upload.mp4")
                            FileOutputStream(file).use { out -> stream.copyTo(out) }
                            val part = MultipartBody.Part.createFormData(
                                "file",
                                file.name,
                                file.asRequestBody(mime.toMediaTypeOrNull())
                            )

                            val response = RetrofitClient.instance.uploadVideo(
                                title = title.toRequestBody("text/plain".toMediaTypeOrNull()),
                                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                                topic = topic.toRequestBody("text/plain".toMediaTypeOrNull()),
                                duration = if (duration.isBlank()) null else duration.toRequestBody("text/plain".toMediaTypeOrNull()),
                                file = part
                            )

                            if (response.isSuccessful) {
                                onUploaded()
                            } else {
                                errorMsg = "No se pudo subir (${response.code()})"
                            }
                        } catch (e: Exception) {
                            errorMsg = "Error de red: ${e.message ?: "desconocido"}"
                        }
                        isUploading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text("Subir video", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
