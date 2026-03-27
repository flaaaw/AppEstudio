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
            FloatingActionButton(
                onClick = { showCreatePost = true },
                containerColor = Emerald500,
                contentColor = Color.White,
                shape = CircleShape
            ) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Comunidad", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        IconButton(
                            onClick = { showSearch = !showSearch },
                            modifier = Modifier.background(Slate800, CircleShape)
                        ) {
                            Icon(if (showSearch) Icons.Default.Close else Icons.Default.Search, contentDescription = "Buscar", tint = Emerald400, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Search
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it 
                                // Simplified server search: empty = load all, else search
                                scope.launch { feedViewModel.searchPosts(it) }
                            },
                            placeholder = { Text("Buscar publicaciones...", color = Slate500) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Emerald500) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald500,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Filters
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        items(listOf("Todos", "Dudas", "Material", "Programación", "Matemáticas")) { item ->
                            val isSelected = filter == item
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(if (isSelected) Emerald500 else Slate800)
                                    .border(1.dp, if (isSelected) Emerald400 else Slate700, RoundedCornerShape(30.dp))
                                    .clickable { 
                                        filter = item
                                        if (item == "Todos") feedViewModel.loadPosts()
                                        // Filter locally for now, server search is via query
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(item, color = if (isSelected) Color.White else Slate300, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                when (val state = uiState) {
                    is FeedUiState.Loading -> {
                        items(5) { ShimmerPostItem() }
                    }
                    is FeedUiState.Error -> {
                        item {
                            Column(modifier = Modifier.fillParentMaxSize().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.CloudOff, contentDescription = null, tint = Slate500, modifier = Modifier.size(60.dp))
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(state.message, color = Slate400, fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = { feedViewModel.loadPosts() }, colors = ButtonDefaults.buttonColors(containerColor = Emerald500), shape = RoundedCornerShape(12.dp)) {
                                    Text("Intentar de nuevo")
                                }
                            }
                        }
                    }
                    is FeedUiState.Success -> {
                        val filtered = state.posts.filter { post ->
                            val matchesFilter = filter == "Todos" || post.tags.any { it.equals(filter, ignoreCase = true) }
                            // Server already filters search if query is sent, but we keep local filter for UI consistency
                            matchesFilter
                        }

                        if (filtered.isEmpty() && !isLoading) {
                            item {
                                Column(modifier = Modifier.fillParentMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.Topic, null, tint = Slate700, modifier = Modifier.size(80.dp).padding(bottom = 16.dp))
                                    Text("¡Aún no hay nada aquí!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("Sé el primero en compartir algo interesante.", color = Slate500, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
                                        } catch (_: Exception) {}
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
                    model = post.mediaUrl,
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
                    Icon(Icons.AutoMirrored.Outlined.ChatBubbleOutline, contentDescription = null, tint = Slate500, modifier = Modifier.size(22.dp))
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
