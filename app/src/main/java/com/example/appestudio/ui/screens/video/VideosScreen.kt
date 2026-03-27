package com.example.appestudio.ui.screens.video

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.appestudio.data.SessionManager
import com.example.appestudio.data.models.VideoDto
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.ui.theme.*
import com.example.appestudio.utils.toRelativeTime

private val TOPICS = listOf("Todos", "Programación", "Matemáticas", "Química", "Física", "General")

@Composable
fun VideosScreen(navController: NavController, sessionManager: SessionManager? = null) {
    var selectedTopic by remember { mutableStateOf("Todos") }
    var videos by remember { mutableStateOf<List<VideoDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var playingVideoUrl by remember { mutableStateOf<String?>(null) }

    suspend fun loadVideos() {
        isLoading = true; errorMsg = null
        try {
            val topic = if (selectedTopic == "Todos") null else selectedTopic
            val resp = RetrofitClient.instance.getVideos(topic)
            if (resp.isSuccessful) videos = resp.body() ?: emptyList()
            else errorMsg = "Error al cargar videos (${resp.code()})"
        } catch (_: Exception) { errorMsg = "Sin conexión al servidor" }
        isLoading = false
    }

    LaunchedEffect(selectedTopic) { loadVideos() }

    LazyColumn(modifier = Modifier.fillMaxSize().background(Slate900), contentPadding = PaddingValues(bottom = 100.dp, top = 24.dp)) {
        item {
            Text("Videos", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))

            // Topic filter chips
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                items(TOPICS) { topic ->
                    val selected = topic == selectedTopic
                    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected) Emerald500 else Slate800).clickable { selectedTopic = topic }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(topic, color = if (selected) Color.White else Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        when {
            isLoading -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald500)
                }
            }
            errorMsg != null -> item {
                Column(modifier = Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = Slate500, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMsg!!, color = Slate400, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { selectedTopic = selectedTopic }, colors = ButtonDefaults.buttonColors(containerColor = Emerald500)) { Text("Reintentar") }
                }
            }
            videos.isEmpty() -> item {
                Column(modifier = Modifier.fillMaxWidth().padding(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Slate600, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No hay videos disponibles aún", color = Slate500, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Los profesores pueden subir videos desde la plataforma web", color = Slate600, fontSize = 12.sp)
                }
            }
            else -> items(videos) { video ->
                VideoCard(video = video, onClick = { playingVideoUrl = it })
            }
        }
    }

    // Full screen video overlay
    if (playingVideoUrl != null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black).clickable(enabled = false) {}
        ) {
            VideoPlayer(
                videoUrl = playingVideoUrl!!,
                modifier = Modifier.fillMaxSize().align(Alignment.Center)
            )
            
            // Close button
            IconButton(
                onClick = { playingVideoUrl = null },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
        }
    }
}

@Composable
fun VideoCard(video: VideoDto, onClick: (String) -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)).background(Slate800)
            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick(video.videoUrl) }
    ) {
        Column {
            // Thumbnail
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))) {
                if (!video.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(model = video.thumbnailUrl, contentDescription = video.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Slate700), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Slate600, modifier = Modifier.size(56.dp))
                    }
                }
                // Duration badge
                if (video.duration.isNotBlank()) {
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(video.duration, color = Color.White, fontSize = 11.sp)
                    }
                }
                // Topic badge
                Box(modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Emerald500.copy(alpha = 0.9f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(video.topic, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Info
            Column(modifier = Modifier.padding(16.dp)) {
                Text(video.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(video.uploaderName, color = Slate400, fontSize = 12.sp)
                    Text("·", color = Slate600, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Slate600, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("${video.views}", color = Slate600, fontSize = 12.sp)
                    }
                    Text("·", color = Slate600, fontSize = 12.sp)
                    Text(video.createdAt.toRelativeTime(), color = Slate600, fontSize = 12.sp)
                }
            }
        }
    }
}
