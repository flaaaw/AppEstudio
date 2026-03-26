package com.example.appestudio.ui.screens.feed

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.ui.theme.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostModal(
    authorName: String,
    sessionManager: com.example.appestudio.data.SessionManager? = null,
    onDismiss: () -> Unit,
    onPostCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(idx)
            } ?: "archivo"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nueva Publicación", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                }
            }

            errorMsg?.let {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    .background(Red500.copy(alpha=0.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, Red500.copy(alpha=0.3f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Text(it, color = Red500, fontSize = 13.sp)
                }
            }

            // Title
            Text("Título", color = Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                placeholder = { Text("Ej. Duda sobre matrices...", color = Slate500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500, unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800, unfocusedContainerColor = Slate800
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            Text("Contenido", color = Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = content, onValueChange = { content = it },
                placeholder = { Text("Escribe tu publicación aquí...", color = Slate500) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500, unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800, unfocusedContainerColor = Slate800
                ),
                minLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tags
            Text("Etiquetas (separadas por coma)", color = Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = tags, onValueChange = { tags = it },
                placeholder = { Text("Ej. Matemáticas, Duda", color = Slate500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500, unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800, unfocusedContainerColor = Slate800
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // File Picker
            OutlinedButton(
                onClick = { filePicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedFileUri != null) Emerald500 else Slate700),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (selectedFileUri != null) Emerald400 else Slate400, containerColor = Slate800)
            ) {
                Icon(
                    if (selectedFileUri != null) Icons.Default.CheckCircle else Icons.Default.AttachFile,
                    contentDescription = null, modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedFileName ?: "Adjuntar archivo (imagen, PDF, audio)", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        errorMsg = "El título y el contenido son requeridos"
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        errorMsg = null
                        try {
                            val authorBody   = authorName.toRequestBody("text/plain".toMediaTypeOrNull())
                            val authorIdBody = (sessionManager?.getUserId() ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                            val contentBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
                            val tagsBody = tags.toRequestBody("text/plain".toMediaTypeOrNull())

                            val filePart = selectedFileUri?.let { uri ->
                                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                                val stream = context.contentResolver.openInputStream(uri)!!
                                val tmpFile = File(context.cacheDir, selectedFileName ?: "upload")
                                FileOutputStream(tmpFile).use { out -> stream.copyTo(out) }
                                val reqBody = tmpFile.asRequestBody(mime.toMediaTypeOrNull())
                                MultipartBody.Part.createFormData("file", tmpFile.name, reqBody)
                            }

                            val response = RetrofitClient.instance.createPost(
                                authorBody, titleBody, contentBody, tagsBody, filePart
                            )
                            if (response.isSuccessful) {
                                onPostCreated()
                            } else {
                                errorMsg = "Error al publicar (${response.code()})"
                            }
                        } catch (e: Exception) {
                            errorMsg = "Sin conexión al servidor"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publicar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
