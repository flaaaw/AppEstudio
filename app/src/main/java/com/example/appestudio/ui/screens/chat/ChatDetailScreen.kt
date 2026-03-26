package com.example.appestudio.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appestudio.data.SessionManager
import com.example.appestudio.data.models.MessageDto
import com.example.appestudio.data.models.SendMessageRequest
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.ui.theme.*
import com.example.appestudio.utils.toRelativeTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatId: String?,
    chatName: String = "Chat",
    sessionManager: SessionManager? = null
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messageInput by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }

    val myId = sessionManager?.getUserId() ?: ""
    val myName = sessionManager?.getName() ?: "Yo"

    suspend fun fetchMessages() {
        if (chatId.isNullOrBlank()) { isLoading = false; return }
        try {
            val response = RetrofitClient.instance.getMessages(chatId)
            if (response.isSuccessful) {
                val newMessages = response.body() ?: emptyList()
                if (newMessages.size != messages.size) {
                    messages = newMessages
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
        } catch (_: Exception) {}
        isLoading = false
    }

    // Auto-poll every 5 seconds
    LaunchedEffect(chatId) {
        while (true) {
            fetchMessages()
            delay(5_000)
        }
    }

    fun sendMessage() {
        val text = messageInput.trim()
        if (text.isBlank() || chatId.isNullOrBlank()) return
        messageInput = ""
        scope.launch {
            isSending = true
            try {
                RetrofitClient.instance.sendMessage(
                    chatId = chatId,
                    request = SendMessageRequest(senderId = myId, senderName = myName, text = text)
                )
                fetchMessages()
            } catch (_: Exception) {}
            isSending = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Slate900)) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().background(Slate800).padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Slate700), contentAlignment = Alignment.Center) {
                Text(chatName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    java.net.URLDecoder.decode(chatName, "UTF-8"),
                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text("${messages.size} mensajes", color = Slate400, fontSize = 12.sp)
            }
        }

        // Messages
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald500)
                }
            } else if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Slate600, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sé el primero en escribir", color = Slate500, fontSize = 14.sp)
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
                                    .clip(RoundedCornerShape(
                                        topStart = 18.dp, topEnd = 18.dp,
                                        bottomStart = if (isMine) 18.dp else 4.dp,
                                        bottomEnd = if (isMine) 4.dp else 18.dp
                                    ))
                                    .background(if (isMine) Emerald600 else Slate700)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(msg.text, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                            }
                            Text(
                                msg.createdAt.toRelativeTime(),
                                color = Slate600, fontSize = 10.sp,
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Attach menu
        AnimatedVisibility(visible = showAttachMenu, enter = fadeIn() + slideInVertically { it }, exit = fadeOut() + slideOutVertically { it }) {
            Row(modifier = Modifier.fillMaxWidth().background(Slate800).padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("Imagen" to Icons.Default.Image, "Documento" to Icons.Default.InsertDriveFile, "Audio" to Icons.Default.Mic).forEach { (label, icon) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Slate700), contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = label, tint = Emerald400, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(label, color = Slate400, fontSize = 12.sp)
                    }
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth().background(Slate800).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showAttachMenu = !showAttachMenu }) {
                Icon(if (showAttachMenu) Icons.Default.Close else Icons.Default.AttachFile, contentDescription = "Attach", tint = if (showAttachMenu) Emerald400 else Slate400)
            }
            TextField(
                value = messageInput, onValueChange = { messageInput = it },
                placeholder = { Text("Escribe un mensaje...", color = Slate500) },
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Slate700, unfocusedContainerColor = Slate700,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { sendMessage() },
                enabled = messageInput.isNotBlank() && !isSending,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(if (messageInput.isNotBlank()) Emerald500 else Slate700)
            ) {
                if (isSending) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}
