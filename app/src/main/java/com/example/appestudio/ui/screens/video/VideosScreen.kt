package com.example.appestudio.ui.screens.video

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
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
import coil.compose.AsyncImage
import com.example.appestudio.data.SessionManager
import com.example.appestudio.data.models.VideoDto
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.ui.theme.*
import com.example.appestudio.utils.toRelativeTime
import kotlinx.coroutines.launch

@Composable
fun VideosScreen(navController: NavController, sessionManager: SessionManager? = null) {
    val scope = rememberCoroutineScope()
    var dynamicTopics by remember { mutableStateOf(listOf("Todos")) }
    var selectedTopic by remember { mutableStateOf("Todos") }
    var videos by remember { mutableStateOf<List<VideoDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var playingVideoUrl by remember { mutableStateOf<String?>(null) }
    var showUploadModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getVideoStats()
            if (response.isSuccessful) {
                val stats = response.body() ?: emptyList()
                val tags = mutableListOf("Todos")
                tags.addAll(stats.take(6).map { it._id })
                if (tags.size > 1) {
                    dynamicTopics = tags
                }
            }
        } catch (_: Exception) {}
    }

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

    if (showUploadModal) {
        UploadVideoModal(
            onDismiss = { showUploadModal = false },
            onUploaded = {
                showUploadModal = false
                scope.launch { loadVideos() }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadModal = true },
                containerColor = Emerald500,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = "Subir video")
            }
        },
        containerColor = Slate900
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Slate900).padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp, top = 24.dp)
        ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Clases Grabadas", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("Aprende a tu propio ritmo", color = Slate400, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Topic filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                items(dynamicTopics) { topic ->
                    val selected = topic == selectedTopic
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .clickable { selectedTopic = topic },
                        color = if (selected) Emerald500 else Slate800,
                        border = BorderStroke(1.dp, if (selected) Emerald400 else Slate700)
                    ) {
                        Text(
                            topic,
                            color = if (selected) Color.White else Slate300,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        when {
            isLoading -> item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald500, strokeWidth = 3.dp)
                }
            }
            errorMsg != null -> item {
                Column(
                    modifier = Modifier.fillParentMaxSize().padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Red500, modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(errorMsg!!, color = Slate400, fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { scope.launch { loadVideos() } },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Reintentar") }
                }
            }
            videos.isEmpty() -> item {
                Column(
                    modifier = Modifier.fillParentMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Slate700, modifier = Modifier.size(80.dp).padding(bottom = 16.dp))
                    Text("Aún no hay videos aquí", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Los tutores subirán material pronto para este tema.", color = Slate500, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            else -> items(videos) { video ->
                VideoCard(
                    video = video,
                    onClick = { url ->
                        playingVideoUrl = url
                        scope.launch { RetrofitClient.instance.incrementView(video._id) }
                    }
                )
            }
        }
        }
    }

    // Full screen video overlay with premium feel
    if (playingVideoUrl != null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).clickable(enabled = false) {}
        ) {
            VideoPlayer(
                videoUrl = playingVideoUrl!!,
                modifier = Modifier.fillMaxSize().align(Alignment.Center)
            )
            
            // Close button — Elevated with blur-like feel
            Surface(
                onClick = { playingVideoUrl = null },
                modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
                shape = CircleShape,
                color = Slate900.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Slate700)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.padding(12.dp).size(20.dp))
            }
        }
    }
}

@Composable
fun VideoCard(video: VideoDto, onClick: (String) -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clickable { onClick(video.videoUrl) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Slate800),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Thumbnail with Play Button Overlay
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                if (!video.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Slate700), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Slate600, modifier = Modifier.size(60.dp))
                    }
                }
                
                // Play Icon Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }

                // Duration badge
                if (video.duration.isNotBlank()) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            video.duration,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                // Topic badge
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    color = Emerald500.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        video.topic,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Video Info
            Column(modifier = Modifier.padding(20.dp)) {
                Text(video.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2)
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Placeholder for Uploader Avatar
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Slate700), contentAlignment = Alignment.Center) {
                        Text(video.uploaderName.take(1).uppercase(), color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(video.uploaderName, color = Slate400, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.FiberManualRecord, null, tint = Slate600, modifier = Modifier.size(4.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, null, tint = Slate600, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${video.views}", color = Slate500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(video.createdAt.toRelativeTime(), color = Slate600, fontSize = 11.sp, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}
