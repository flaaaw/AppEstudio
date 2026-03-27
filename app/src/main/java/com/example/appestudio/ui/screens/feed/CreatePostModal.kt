package com.example.appestudio.ui.screens.feed

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.appestudio.utils.ImageUtils
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
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Slate700)
            )
        },
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Nueva Publicación", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Comparte tus dudas o material", color = Slate500, fontSize = 13.sp)
                }
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = Slate800,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400, modifier = Modifier.size(20.dp))
                    }
                }
            }

            errorMsg?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = Red500.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Red500.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Red500, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(it, color = Red500, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Title
            Text("Título del Post", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = title, 
                onValueChange = { title = it },
                placeholder = { Text("Ej: Ayuda con Cálculo III", color = Slate500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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

            Spacer(modifier = Modifier.height(20.dp))

            // Content
            Text("Contenido o Pregunta", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = content, 
                onValueChange = { content = it },
                placeholder = { Text("Describe tu duda con detalle...", color = Slate500) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                minLines = 5
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tags
            Text("Etiquetas", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = tags, 
                onValueChange = { tags = it },
                placeholder = { Text("matemáticas, duda, examen...", color = Slate500) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocalOffer, null, tint = Emerald500, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(16.dp),
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

            Spacer(modifier = Modifier.height(24.dp))

            // File Picker
            Surface(
                onClick = { filePicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Slate800,
                border = BorderStroke(1.dp, if (selectedFileUri != null) Emerald500 else Slate700.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(if (selectedFileUri != null) Emerald500.copy(alpha = 0.15f) else Slate700.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (selectedFileUri != null) Icons.Default.CheckCircle else Icons.Default.Share,
                            contentDescription = null, 
                            tint = if (selectedFileUri != null) Emerald500 else Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (selectedFileUri != null) "Archivo seleccionado" else "Adjuntar material", 
                            color = Color.White, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            selectedFileName ?: "Imagen, PDF o Documento", 
                            color = Slate500, 
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                    if (selectedFileUri != null) {
                        IconButton(onClick = { selectedFileUri = null; selectedFileName = null }) {
                            Icon(Icons.Default.Delete, null, tint = Red500, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Post Button
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        errorMsg = "Completa los campos obligatorios"
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
                                val tmpFile = if (mime.startsWith("image/")) {
                                    ImageUtils.compressImage(context, uri, selectedFileName ?: "upload.jpg")
                                } else {
                                    val stream = context.contentResolver.openInputStream(uri)!!
                                    val f = File(context.cacheDir, selectedFileName ?: "upload")
                                    FileOutputStream(f).use { out -> stream.copyTo(out) }
                                    f
                                }
                                
                                if (tmpFile != null) {
                                    val reqBody = tmpFile.asRequestBody(mime.toMediaTypeOrNull())
                                    MultipartBody.Part.createFormData("file", tmpFile.name, reqBody)
                                } else null
                            }

                            val response = RetrofitClient.instance.createPost(
                                authorBody, authorIdBody, titleBody, contentBody, tagsBody, filePart
                            )
                            if (response.isSuccessful) {
                                onPostCreated()
                            } else {
                                errorMsg = "Falló la publicación: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMsg = "Error de red: ${e.message}"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Publicar Ahora", fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
