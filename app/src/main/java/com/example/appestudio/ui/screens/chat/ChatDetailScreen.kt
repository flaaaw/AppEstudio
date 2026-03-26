package com.example.appestudio.ui.screens.chat

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appestudio.data.SessionManager
import com.example.appestudio.data.models.MessageDto
import com.example.appestudio.data.models.SendMessageRequest
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.ui.theme.*
import com.example.appestudio.utils.toRelativeTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatId: String?,
    chatName: String = "Chat",
    sessionManager: SessionManager? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messageInput by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }

    // Selected file for sending
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }

    val myId = sessionManager?.getUserId() ?: ""
    val myName = sessionManager?.getName() ?: "Yo"
    val displayName = try { java.net.URLDecoder.decode(chatName, "UTF-8") } catch (_: Exception) { chatName }

    // File picker
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedUri = it
            selectedMimeType = context.contentResolver.getType(it) ?: "application/octet-stream"
            selectedFileName = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst(); cursor.getString(idx)
            } ?: "archivo"
            showAttachMenu = false
        }
    }

    suspend fun fetchMessages() {
        if (chatId.isNullOrBlank()) { isLoading = false; return }
        try {
            val response = RetrofitClient.instance.getMessages(chatId)
            if (response.isSuccessful) {
                val newMessages = response.body() ?: emptyList()
                if (newMessages.size != messages.size) {
                    messages = newMessages
                    // The scroll logic is now handled by a separate LaunchedEffect
                }
            }
        } catch (_: Exception) {}
        isLoading = false
    }

    // Auto-poll every 5s
    LaunchedEffect(chatId) {
        while (true) { fetchMessages(); delay(5_000) }
    }

    // Scroll to the last message when messages list changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        if (chatId.isNullOrBlank()) return
        val text = messageInput.trim()
        val uri = selectedUri

        if (text.isBlank() && uri == null) return
        messageInput = ""
        selectedUri = null
        selectedFileName = null

        scope.launch {
            isSending = true
            try {
                if (uri != null) {
                    // ── Send with file attachment ──────────────────────────
                    val mime = selectedMimeType ?: "application/octet-stream"
                    val mediaType = when {
                        mime.startsWith("image") -> "image"
                        mime.startsWith("audio") -> "audio"
                        else -> "document"
                    }
                    val stream = context.contentResolver.openInputStream(uri)!!
                    val tmp = File(context.cacheDir, selectedFileName ?: "upload")
                    FileOutputStream(tmp).use { out -> stream.copyTo(out) }
                    val reqFile = tmp.asRequestBody(mime.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", tmp.name, reqFile)

                    RetrofitClient.instance.sendMediaMessage(
                        chatId = chatId,
                        senderId   = myId.toRequestBody("text/plain".toMediaTypeOrNull()),
                        senderName = myName.toRequestBody("text/plain".toMediaTypeOrNull()),
                        text       = (if (text.isBlank()) null else text.toRequestBody("text/plain".toMediaTypeOrNull())),
                        mediaType  = mediaType.toRequestBody("text/plain".toMediaTypeOrNull()),
                        file       = part
                    )
                } else {
                    // ── Text only ──────────────────────────────────────────
                    RetrofitClient.instance.sendMessage(
                        chatId = chatId,
                        request = SendMessageRequest(senderId = myId, senderName = myName, text = text)
                    )
                }
                fetchMessages()
            } catch (_: Exception) {}
            isSending = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Slate900)) {
        // ── Top Bar ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().background(Slate800).padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Slate700), contentAlignment = Alignment.Center) {
                Text(displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text("${messages.size} mensajes", color = Slate400, fontSize = 12.sp)
            }
        }

        // ── Messages ──────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald500)
                }
            } else if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = Slate600, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Inicia la conversación", color = Slate500, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isMine = msg.senderId == myId
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                        ) {
                            if (!isMine) {
                                Text(msg.senderName, color = Emerald400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = if (isMine) 18.dp else 4.dp, bottomEnd = if (isMine) 4.dp else 18.dp))
                                    .background(if (isMine) Emerald600 else Slate700)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    if (!msg.mediaUrl.isNullOrBlank()) {
                                        val isImage = msg.mediaType == "image" || msg.mediaUrl.contains("cloudinary") && !msg.mediaUrl.endsWith(".pdf")
                                        if (isImage) {
                                            AsyncImage(
                                                model = msg.mediaUrl,
                                                contentDescription = "Imagen",
                                                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            if (msg.text.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                                        } else {
                                            Row(
                                                modifier = Modifier.padding(bottom = if (msg.text.isNotBlank()) 6.dp else 0.dp)
                                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(if (msg.mediaType == "audio") Icons.Default.Mic else Icons.Default.InsertDriveFile, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Archivo adjunto", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                            }
                                        }
                                    }
                                    if (msg.text.isNotBlank()) {
                                        Text(msg.text, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                                    }
                                }
                            }
                            Text(msg.createdAt.toRelativeTime(), color = Slate600, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp))
                        }
                    }
                }
            }
        }

        // ── File preview bar (when a file is selected) ────────────────
        AnimatedVisibility(visible = selectedUri != null, enter = fadeIn() + slideInVertically { it }, exit = fadeOut() + slideOutVertically { it }) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Slate800).border(width = 1.dp, color = Slate700).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AttachFile, null, tint = Emerald400, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedFileName ?: "Archivo seleccionado", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1)
                IconButton(onClick = { selectedUri = null; selectedFileName = null }) {
                    Icon(Icons.Default.Close, null, tint = Slate500, modifier = Modifier.size(18.dp))
                }
            }
        }

        // ── Attach menu ───────────────────────────────────────────────
        AnimatedVisibility(visible = showAttachMenu, enter = fadeIn() + slideInVertically { it }, exit = fadeOut() + slideOutVertically { it }) {
            Row(modifier = Modifier.fillMaxWidth().background(Slate800).padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                AttachOption("Imagen", Icons.Default.Image) { filePicker.launch("image/*") }
                AttachOption("Documento", Icons.Default.InsertDriveFile) { filePicker.launch("application/*") }
                AttachOption("Audio", Icons.Default.Mic) { filePicker.launch("audio/*") }
            }
        }

        // ── Input bar ─────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().background(Slate800).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showAttachMenu = !showAttachMenu }) {
                Icon(if (showAttachMenu) Icons.Default.Close else Icons.Default.AttachFile, null, tint = if (showAttachMenu) Emerald400 else Slate400)
            }
            TextField(
                value = messageInput, onValueChange = { messageInput = it },
                placeholder = { Text(if (selectedUri != null) "Añadir un texto..." else "Escribe un mensaje...", color = Slate500) },
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Slate700, unfocusedContainerColor = Slate700,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            val canSend = (messageInput.isNotBlank() || selectedUri != null) && !isSending
            IconButton(
                onClick = { sendMessage() },
                enabled = canSend,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(if (canSend) Emerald500 else Slate700)
            ) {
                if (isSending) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun AttachOption(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Slate700), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = Emerald400, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = Slate400, fontSize = 12.sp)
    }
}
