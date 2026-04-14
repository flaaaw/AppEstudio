package com.example.appestudio.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import com.example.appestudio.data.models.PostDto
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.ui.theme.*
import com.example.appestudio.utils.toRelativeTime
import kotlinx.coroutines.launch

@Composable
fun PostCard(
    navController: NavController,
    post: PostDto,
    currentUserId: String = "",
    onDelete: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLiked by remember(post._id) { mutableStateOf(post.likedBy.contains(currentUserId)) }
    var likesCount by remember(post._id) { mutableStateOf(post.likes) }
    var showMenu by remember { mutableStateOf(false) }
    val isOwner = post.authorId == currentUserId && currentUserId.isNotBlank()

    val isImageUrl = !post.mediaUrl.isNullOrBlank() &&
        (post.mediaUrl.contains("cloudinary") || post.mediaUrl.endsWith(".jpg") || post.mediaUrl.endsWith(".png") || post.mediaUrl.endsWith(".jpeg") || post.mediaUrl.endsWith(".webp"))

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Slate800),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Author Avatar
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Emerald500, Emerald700))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(post.author.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.author, color = Color.LightGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, tint = Slate500, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(post.createdAt.toRelativeTime(), color = Slate500, fontSize = 11.sp)
                    }
                }
                if (isOwner) {
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Slate400)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = Slate800) {
                            DropdownMenuItem(
                                text = { Text("Eliminar publicación", color = Red500) },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Red500) },
                                onClick = {
                                    showMenu = false
                                    scope.launch {
                                        try {
                                            val resp = RetrofitClient.instance.deletePost(post._id, com.example.appestudio.data.models.DeletePostRequest(currentUserId))
                                            if (resp.isSuccessful) onDelete()
                                        } catch (e: Exception) {
                                            Log.e("PostCard", "Error deleting post", e)
                                            Toast.makeText(context, "No se pudo eliminar el post", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(post.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(post.content, color = Slate200, fontSize = 15.sp, lineHeight = 22.sp)

            // Dynamic Content Placement
            if (isImageUrl) {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = post.mediaUrl?.replace("http://", "https://"),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Slate700.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (!post.mediaUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Emerald500.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = Emerald400, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Documento adjunto", color = Emerald400, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            if (post.tags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(post.tags) { tag ->
                        Surface(
                            color = Slate900.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Slate700)
                        ) {
                            Text(
                                "#$tag",
                                color = Emerald400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            HorizontalDivider(color = Slate700.copy(alpha = 0.4f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    scope.launch {
                        try {
                            val resp = RetrofitClient.instance.likePost(post._id, mapOf("userId" to currentUserId))
                            if (resp.isSuccessful) {
                                val body = resp.body()!!
                                isLiked = body.liked
                                likesCount = body.likes
                            }
                        } catch (_: Exception) {
                            isLiked = !isLiked
                            likesCount += if (isLiked) 1 else -1
                        }
                    }
                }.padding(8.dp)) {
                    Icon(
                        if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isLiked) Red500 else Slate500,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(likesCount.toString(), color = if (isLiked) Color.White else Slate500, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    navController.navigate("comments/${post._id}")
                }.padding(8.dp)) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(post.comments.toString(), color = Slate500, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                
                IconButton(onClick = {
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, post.title)
                        putExtra(android.content.Intent.EXTRA_TEXT, "${post.title}\n\n${post.content}")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir via"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Slate500, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}
