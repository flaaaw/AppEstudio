package com.example.appestudio.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.appestudio.data.models.CreateChatRequest
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.ui.theme.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class UserSearchResult(
    val id: String,
    val name: String,
    val email: String,
    val career: String
)

@Composable
fun NewChatScreen(
    navController: NavController,
    sessionManager: SessionManager? = null
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<UserSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Debounced search
    LaunchedEffect(searchQuery) {
        delay(500)
        if (searchQuery.length < 2) { results = emptyList(); return@LaunchedEffect }
        isSearching = true
        try {
            val response = RetrofitClient.instance.searchUsers(searchQuery)
            if (response.isSuccessful) {
                results = response.body()?.map {
                    UserSearchResult(
                        id = it._id,
                        name = it.name,
                        email = it.email,
                        career = it.career
                    )
                } ?: emptyList()
            }
        } catch (_: Exception) {
            errorMsg = "Sin conexión"
        }
        isSearching = false
    }

    Column(modifier = Modifier.fillMaxSize().background(Slate900)) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().background(Slate800).padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Nuevo Mensaje", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Column(modifier = Modifier.padding(24.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre o correo...", color = Slate500) },
                leadingIcon = {
                    if (isSearching) CircularProgressIndicator(color = Emerald500, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Search, contentDescription = null, tint = Slate500)
                },
                modifier = Modifier.fillMaxWidth().background(Slate800, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500, unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800, unfocusedContainerColor = Slate800
                ),
                singleLine = true
            )
        }

        errorMsg?.let {
            Text(it, color = Red500, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 24.dp))
        }

        if (searchQuery.length < 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Slate600, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Escribe al menos 2 caracteres\npara buscar usuarios", color = Slate500, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else if (results.isEmpty() && !isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron usuarios", color = Slate400, fontSize = 14.sp)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)) {
                items(results) { user ->
                    // Don't show yourself
                    if (user.id == sessionManager?.getUserId()) return@items

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate800)
                            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                if (isCreating) return@clickable
                                scope.launch {
                                    isCreating = true
                                    try {
                                        val response = RetrofitClient.instance.createChat(
                                            CreateChatRequest(
                                                userId   = sessionManager?.getUserId() ?: "",
                                                userId2  = user.id,
                                                userName = sessionManager?.getName() ?: "",
                                                userName2 = user.name
                                            )
                                        )
                                        if (response.isSuccessful) {
                                            val chatId = response.body()?._id
                                            val encodedName = java.net.URLEncoder.encode(user.name, "UTF-8")
                                            navController.navigate("chat/$chatId/$encodedName") {
                                                popUpTo("new_chat") { inclusive = true }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        errorMsg = "Error al crear el chat"
                                    }
                                    isCreating = false
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Slate700), contentAlignment = Alignment.Center) {
                            Text(user.name.take(1).uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(user.career, color = Slate400, fontSize = 12.sp)
                            Text(user.email, color = Slate600, fontSize = 11.sp)
                        }
                        if (isCreating) CircularProgressIndicator(color = Emerald500, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate500)
                    }
                }
            }
        }
    }
}
