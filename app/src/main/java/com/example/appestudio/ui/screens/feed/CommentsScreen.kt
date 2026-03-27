package com.example.appestudio.ui.screens.feed

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    navController: NavController,
    postId: String,
    sessionManager: SessionManager? = null
) {
    val context = LocalContext.current
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
        } catch (e: Exception) {
            Log.e("CommentsScreen", "Error loading comments", e)
            Toast.makeText(context, "No se pudieron cargar los comentarios", Toast.LENGTH_SHORT).show()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Comentarios", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Comparte tu opinión", color = Slate500, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = Slate900
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Comments List
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald500, strokeWidth = 3.dp)
                }
            } else if (comments.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = Slate700, modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Aún no hay comentarios", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Sé el primero en decir algo sobre esta publicación.", color = Slate500, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f), 
                    contentPadding = PaddingValues(24.dp), 
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(comments) { comment ->
                        CommentItem(comment)
                    }
                }
            }

            // Redesigned Input field
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Slate800,
                tonalElevation = 12.dp,
                border = BorderStroke(1.dp, Slate700.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Escribe un mensaje...", color = Slate500, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald500,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate900.copy(alpha = 0.5f),
                            unfocusedContainerColor = Slate900.copy(alpha = 0.5f)
                        ),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = if (commentText.isNotBlank()) Emerald500 else Slate700,
                        enabled = commentText.isNotBlank() && !isSending,
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
                                    } catch (e: Exception) {
                                        Log.e("CommentsScreen", "Error sending comment", e)
                                        Toast.makeText(context, "No se pudo enviar el comentario", Toast.LENGTH_SHORT).show()
                                    }
                                    isSending = false
                                }
                            }
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSending) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentDto) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Avatar with Premium Gradient base
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Slate700, Slate800))),
            contentAlignment = Alignment.Center
        ) {
            if (!comment.authorAvatar.isNullOrBlank()) {
                AsyncImage(
                    model = comment.authorAvatar,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    comment.authorName.take(1).uppercase(), 
                    color = Color.White, 
                    fontWeight = FontWeight.Black, 
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(comment.authorName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                Text(comment.createdAt.toRelativeTime(), color = Slate500, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Slate800.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                border = BorderStroke(1.dp, Slate700.copy(alpha = 0.3f))
            ) {
                Text(
                    comment.content, 
                    color = Slate300, 
                    fontSize = 14.sp, 
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}
