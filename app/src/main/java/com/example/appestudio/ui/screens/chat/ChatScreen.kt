package com.example.appestudio.ui.screens.chat

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appestudio.data.SessionManager
import com.example.appestudio.data.models.ChatDto
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.navigation.Screen
import com.example.appestudio.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // Auto-refresh every 10s to pick up new last messages
    LaunchedEffect(userId) {
        while (true) {
            delay(10_000)
            if (userId.isNotBlank()) {
                try {
                    val response = RetrofitClient.instance.getChats(userId)
                    if (response.isSuccessful) { chats = response.body() ?: emptyList() }
                } catch (_: Exception) {}
            }
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
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Mensajes", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar chats...", color = Slate500) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                modifier = Modifier.fillMaxWidth().background(Slate800, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
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
                    CircularProgressIndicator(color = Emerald500)
                }
                errorMsg != null -> Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = Slate500, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMsg!!, color = Slate400, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { loadChats() }, colors = ButtonDefaults.buttonColors(containerColor = Emerald500)) {
                        Text("Reintentar")
                    }
                }
                filtered.isEmpty() -> Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Slate500, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes conversaciones aún", color = Slate400, fontSize = 14.sp)
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
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(if (isGroup) Emerald600.copy(alpha = 0.8f) else Slate700), contentAlignment = Alignment.Center) {
            if (isGroup) Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            else Text(name.take(1).uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (isOnline) Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-2).dp, y = (-2).dp).size(14.dp).clip(CircleShape).background(Emerald500))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(time, color = if (unreadCount > 0) Emerald500 else Slate500, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(message, color = if (unreadCount > 0) Color.White else Slate400, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f).padding(end = 8.dp))
                if (unreadCount > 0) {
                    Box(modifier = Modifier.background(Emerald500, CircleShape).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
