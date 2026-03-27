package com.example.appestudio.ui.screens.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appestudio.data.SessionManager
import com.example.appestudio.data.models.ChatDto
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.data.network.SocketHandler
import com.example.appestudio.navigation.Screen
import com.example.appestudio.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.socket.emitter.Emitter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, sessionManager: SessionManager? = null) {
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    var searchQuery by remember { mutableStateOf("") }
    var chats by remember { mutableStateOf<List<ChatDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val userId = sessionManager?.getUserId() ?: ""

    fun loadChats() {
        if (userId.isBlank()) { isLoading = false; return }
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getChats(userId)
                if (response.isSuccessful) {
                    chats = response.body() ?: emptyList()
                    errorMsg = null
                } else {
                    errorMsg = "Error ${response.code()}"
                }
            } catch (e: Exception) {
                errorMsg = "Sin conexión"
            }
            isLoading = false
        }
    }

    LaunchedEffect(userId) { loadChats() }

    // Auto-refresh fallback every 30s
    LaunchedEffect(userId) {
        while (true) {
            delay(30_000)
            if (userId.isNotBlank()) {
                try {
                    val response = RetrofitClient.instance.getChats(userId)
                    if (response.isSuccessful) { chats = response.body() ?: emptyList() }
                } catch (e: Exception) {
                    Log.e("ChatScreen", "Fallback refresh failed", e)
                }
            }
        }
    }

    DisposableEffect(userId) {
        if (userId.isBlank()) return@DisposableEffect onDispose {}
        val socket = SocketHandler.getSocket()
        val onMessage = Emitter.Listener { loadChats() }
        val onChatUpdated = Emitter.Listener { loadChats() }
        socket?.on("message", onMessage)
        socket?.on("chat_updated", onChatUpdated)
        onDispose {
            socket?.off("message", onMessage)
            socket?.off("chat_updated", onChatUpdated)
        }
    }

    val filtered = chats.filter { it.name.contains(searchQuery, ignoreCase = true) ||
        it.participantNames.any { n -> n.contains(searchQuery, ignoreCase = true) } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.NewChat.route) },
                containerColor = Emerald500,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Nuevo mensaje")
            }
        },
        containerColor = Slate900
    ) { scaffoldPadding ->
    Column(modifier = Modifier.fillMaxSize().background(Slate900).padding(scaffoldPadding)) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("Mis Conversaciones", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("Conéctate con otros estudiantes", color = Slate400, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar mensajes o contactos...", color = Slate500) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Emerald500) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate800.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Transparent,
                        unfocusedContainerColor = Transparent
                    ),
                    singleLine = true
                )
            }

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { loadChats() },
            modifier = Modifier.weight(1f),
            state = pullToRefreshState
        ) {
            when {
                isLoading && chats.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald500, strokeWidth = 3.dp)
                }
                errorMsg != null -> Column(modifier = Modifier.fillMaxSize().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = Slate500, modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(errorMsg!!, color = Slate400, fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { loadChats() }, colors = ButtonDefaults.buttonColors(containerColor = Emerald500), shape = RoundedCornerShape(12.dp)) {
                        Text("Reintentar")
                    }
                }
                filtered.isEmpty() -> Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Slate700, modifier = Modifier.size(80.dp).padding(bottom = 16.dp))
                    Text("¡Sin mensajes aún!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Inicia una charla con tus compañeros para empezar.", color = Slate500, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
                    items(filtered) { chat ->
                        val displayName = if (chat.isGroup) chat.name
                            else chat.participantNames.firstOrNull { it != sessionManager?.getName() } ?: "Chat"
                        ChatItem(
                            name = displayName,
                            message = chat.lastMessage.ifBlank { "Inicia la conversación" },
                            time = chat.lastMessageAt.take(10),
                            unreadCount = 0,
                            isGroup = chat.isGroup,
                            onClick = { navController.navigate("chat/${chat._id}/${java.net.URLEncoder.encode(displayName, "UTF-8")}") }
                        )
                    }
                }
            }
        }
    } // end Column
    } // end Scaffold
}

@Composable
fun ChatItem(
    name: String, message: String, time: String,
    unreadCount: Int, isOnline: Boolean = false, isGroup: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Premium Avatar with Gradient
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGroup) Brush.linearGradient(listOf(Emerald600, Emerald800))
                        else Brush.linearGradient(listOf(Slate700, Slate800))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isGroup) Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                else Text(name.take(1).uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Emerald500)
                            .border(2.dp, Slate900, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(18.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text(time, color = if (unreadCount > 0) Emerald400 else Slate500, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        message, 
                        color = if (unreadCount > 0) Color.White else Slate400, 
                        fontSize = 14.sp, 
                        maxLines = 1, 
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f).padding(end = 12.dp)
                    )
                    if (unreadCount > 0) {
                        Surface(
                            color = Emerald500,
                            shape = CircleShape
                        ) {
                            Text(
                                unreadCount.toString(), 
                                color = Color.White, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate700, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
