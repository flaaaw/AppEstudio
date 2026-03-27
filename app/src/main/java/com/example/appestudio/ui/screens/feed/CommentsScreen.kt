package com.example.appestudio.ui.screens.feed

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appestudio.data.SessionManager
import com.example.appestudio.data.models.CommentDto
import com.example.appestudio.data.models.CreateCommentRequest
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.ui.theme.*
import com.example.appestudio.utils.toRelativeTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    navController: NavController,
    postId: String,
    sessionManager: SessionManager? = null
) {
    val scope = rememberCoroutineScope()
    var comments by remember { mutableStateOf<List<CommentDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        try {
            val resp = RetrofitClient.instance.getComments(postId)
            if (resp.isSuccessful) {
                comments = resp.body() ?: emptyList()
            }
        } catch (_: Exception) {}
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comentarios", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate900
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Comments List
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald500)
                }
            } else if (comments.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = Slate600, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No hay comentarios aún", color = Slate500)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(comments) { comment ->
                        CommentItem(comment)
                    }
                }
            }

            // Input field
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Slate800,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Escribe un comentario...", color = Slate500, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald500,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        ),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank() && !isSending) {
                                scope.launch {
                                    isSending = true
                                    try {
                                        val req = CreateCommentRequest(
                                            authorId = sessionManager?.getUserId() ?: "",
                                            authorName = sessionManager?.getName() ?: "Usuario",
                                            authorAvatar = sessionManager?.getAvatarUrl(),
                                            content = commentText
                                        )
                                        val resp = RetrofitClient.instance.addComment(postId, req)
                                        if (resp.isSuccessful) {
                                            val newComment = resp.body()
                                            if (newComment != null) {
                                                comments = comments + newComment
                                                commentText = ""
                                            }
                                        }
                                    } catch (_: Exception) {}
                                    isSending = false
                                }
                            }
                        },
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(if (commentText.isNotBlank()) Emerald500 else Slate700),
                        enabled = commentText.isNotBlank() && !isSending
                    ) {
                        if (isSending) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        else Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentDto) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (!comment.authorAvatar.isNullOrBlank()) {
            AsyncImage(
                model = comment.authorAvatar,
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Emerald500), contentAlignment = Alignment.Center) {
                Text(comment.authorName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.authorName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(comment.createdAt.toRelativeTime(), color = Slate500, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(comment.content, color = Slate300, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}
