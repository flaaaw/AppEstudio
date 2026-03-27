package com.example.appestudio.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.appestudio.data.SessionManager
import com.example.appestudio.data.models.PostDto
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.ui.components.ShimmerPostItem
import com.example.appestudio.ui.theme.*
import com.example.appestudio.utils.toRelativeTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    sessionManager: SessionManager? = null,
    feedViewModel: FeedViewModel = viewModel(),
    initialTag: String? = null
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val uiState by feedViewModel.uiState.collectAsState()
    var filter by remember { mutableStateOf(initialTag ?: "Todos") }
    var showCreatePost by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val isLoading = uiState is FeedUiState.Loading

    // Infinite scroll detection
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState is FeedUiState.Success) {
            feedViewModel.loadMorePosts()
        }
    }
 
    LaunchedEffect(initialTag) {
        if (initialTag != null) filter = initialTag
    }

    if (showCreatePost) {
        CreatePostModal(
            authorName = sessionManager?.getName() ?: "Usuario",
            sessionManager = sessionManager,
            onDismiss = { showCreatePost = false },
            onPostCreated = { showCreatePost = false; feedViewModel.loadPosts() }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreatePost = true }, containerColor = Emerald500, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Nueva publicación")
            }
        },
        containerColor = Slate900
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { feedViewModel.loadPosts() },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, top = 24.dp)
            ) {
                item {
                    // Header row
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 0.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Comunidad", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(if (showSearch) Icons.Default.Close else Icons.Default.Search, contentDescription = "Buscar", tint = Slate400)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Search
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery, onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar publicaciones...", color = Slate500) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald500, unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedContainerColor = Slate800, unfocusedContainerColor = Slate800
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Filters
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                        items(listOf("Todos", "Dudas", "Material", "Programación", "Matemáticas")) { item ->
                            val isSelected = filter == item
                            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (isSelected) Emerald500 else Slate800).clickable { filter = item }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(item, color = if (isSelected) Color.White else Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                when (val state = uiState) {
                    is FeedUiState.Loading -> {
                        items(5) {
                            ShimmerPostItem()
                        }
                    }
                    is FeedUiState.Error -> {
                        item {
                            Column(modifier = Modifier.fillParentMaxSize().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.WifiOff, contentDescription = null, tint = Slate500, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(state.message, color = Slate400, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { feedViewModel.loadPosts() }, colors = ButtonDefaults.buttonColors(containerColor = Emerald500)) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                    is FeedUiState.Success -> {
                        val filtered = state.posts.filter { post ->
                            val matchesFilter = filter == "Todos" || post.tags.any { it.equals(filter, ignoreCase = true) }
                            val matchesSearch = searchQuery.isBlank() || post.title.contains(searchQuery, ignoreCase = true) || post.content.contains(searchQuery, ignoreCase = true) || post.author.contains(searchQuery, ignoreCase = true)
                            matchesFilter && matchesSearch
                        }

                        if (filtered.isEmpty() && !isLoading) {
                            item {
                                Column(modifier = Modifier.fillParentMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.Inbox, null, tint = Slate600, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No hay publicaciones en esta categoría", color = Slate500, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        } else {
                            items(filtered) { post ->
                                PostCard(
                                    navController = navController,
                                    post = post,
                                    currentUserId = sessionManager?.getUserId() ?: "",
                                    onDelete = { feedViewModel.loadPosts() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(
    navController: androidx.navigation.NavController,
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

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)).background(Slate800)
            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Slate700), contentAlignment = Alignment.Center) {
                    Text(post.author.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.author, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(post.createdAt.toRelativeTime(), color = Slate400, fontSize = 12.sp)
                }
                if (isOwner) {
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = Slate800) {
                            DropdownMenuItem(
                                text = { Text("Eliminar publicación", color = Red500) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Red500) },
                                onClick = {
                                    showMenu = false
                                    scope.launch {
                                        try {
                                            val resp = RetrofitClient.instance.deletePost(post._id, com.example.appestudio.data.models.DeletePostRequest(currentUserId))
                                            if (resp.isSuccessful) onDelete()
                                        } catch (_: Exception) {}
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(post.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(post.content, color = Slate300, fontSize = 14.sp, lineHeight = 20.sp)

            // Image preview (Cloudinary image)
            if (isImageUrl) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Imagen adjunta",
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (!post.mediaUrl.isNullOrBlank()) {
                // Non-image attachment
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.background(Emerald500.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(1.dp, Emerald500.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = Emerald400, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Archivo adjunto", color = Emerald400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (post.tags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(post.tags) { tag ->
                        Box(modifier = Modifier.background(Slate900.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text(tag, color = Emerald400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            HorizontalDivider(color = Slate700.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
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
                            // toggle locally on failure
                            isLiked = !isLiked
                            likesCount += if (isLiked) 1 else -1
                        }
                    }
                }) {
                    Icon(if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (isLiked) Red500 else Slate400, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(likesCount.toString(), color = Slate400, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    navController.navigate("comments/${post._id}")
                }) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(post.comments.toString(), color = Slate400, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, post.title)
                        putExtra(android.content.Intent.EXTRA_TEXT, "${post.title}\n\n${post.content}")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir via"))
                }) {
                    Icon(Icons.Outlined.Share, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir", color = Slate400, fontSize = 14.sp)
                }
            }
        }
    }
}
