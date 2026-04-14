package com.example.appestudio.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appestudio.data.SessionManager
import com.example.appestudio.navigation.Screen
import com.example.appestudio.ui.theme.*
import com.example.appestudio.data.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke

data class Topic(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val count: Int,
    val color: Color,
    val subtopics: List<String>
)

val topics = listOf(
    Topic("math", "Matemáticas", Icons.Default.Calculate, 12, Blue500, listOf("Cálculo Integral", "Álgebra Lineal", "Ecuaciones Diferenciales")),
    Topic("prog", "Programación", Icons.Default.Memory, 24, Emerald500, listOf("Estructuras de Datos", "POO en Java", "Desarrollo Web")),
    Topic("fisica", "Física", Icons.Default.Bolt, 8, Amber500, listOf("Mecánica Clásica", "Electromagnetismo", "Óptica")),
    Topic("quimica", "Química", Icons.Default.Science, 5, Purple500, listOf("Química Orgánica", "Termodinámica")),
    Topic("ingles", "Inglés", Icons.Default.Public, 15, Rose500, listOf("Grammar B1", "Technical Vocabulary", "Speaking Practice")),
    Topic("electronica", "Electrónica", Icons.AutoMirrored.Filled.MenuBook, 9, Cyan500, listOf("Circuitos Digitales", "Microcontroladores"))
)

@Composable
fun DashboardScreen(navController: NavController, sessionManager: SessionManager? = null) {
    val userName = sessionManager?.getName()?.split(" ")?.firstOrNull() ?: "Estudiante"
    var searchQuery by remember { mutableStateOf("") }
    var showNotifications by remember { mutableStateOf(false) }
    var expandedTopicId by remember { mutableStateOf<String?>(null) }
    
    var dynamicTopics by remember { mutableStateOf(topics) }
    
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getPostStats()
            if (response.isSuccessful) {
                val stats = response.body() ?: emptyList()
                val updatedTopics = topics.map { topic ->
                    val apiCount = stats.filter { stat ->
                        stat._id.equals(topic.name, ignoreCase = true) || 
                        topic.subtopics.any { sub -> stat._id.equals(sub, ignoreCase = true) } ||
                        stat._id.equals(topic.id, ignoreCase = true)
                    }.sumOf { it.count }
                    // Update only if we have real counts, otherwise keep showing some activity or 0.
                    // For a realistic app, we show the apiCount. If 0, it means no posts yet.
                    topic.copy(count = apiCount)
                }
                dynamicTopics = updatedTopics
            }
        } catch (_: Exception) {
            // keep default in case of error
        }
    }

    val filteredTopics = dynamicTopics.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.subtopics.any { sub -> sub.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            // Header with Premium Gradient
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Emerald500.copy(alpha = 0.15f), Transparent)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 24.dp, end = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Bienvenido de vuelta,", color = Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("¡Hola, $userName! \uD83D\uDC4B", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { showNotifications = !showNotifications },
                            color = Slate800,
                            border = BorderStroke(1.dp, Slate700)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = Emerald400, modifier = Modifier.size(22.dp))
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Red500)
                                        .border(1.dp, Slate800, CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Redesigned Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar temas, ejercicios...", color = Slate500) },
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
            }
        }

        item {
            // Quick Access Section
            Column(modifier = Modifier.padding(vertical = 32.dp)) {
                Text(
                    "Accesos Rápidos", 
                    color = Color.White, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val quickAccessItems = listOf(
                        Triple("Comunidad", Icons.Default.Groups, Blue500),
                        Triple("Clases", Icons.Default.PlayCircle, Emerald500),
                        Triple("Ejercicios", Icons.Default.Quiz, Violet500)
                    )
                    items(quickAccessItems) { (text, icon, tint) ->
                        Surface(
                            modifier = Modifier
                                .width(150.dp)
                                .height(110.dp)
                                .clickable {
                                    val targetRoute = if (text == "Clases") Screen.Videos.route else "feed"
                                    navController.navigate(targetRoute) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            shape = RoundedCornerShape(24.dp),
                            color = Slate800,
                            border = BorderStroke(1.dp, Slate700.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = tint.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Explorar Temas", 
                color = Color.White, 
                fontSize = 20.sp, 
                fontWeight = FontWeight.ExtraBold, 
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        if (filteredTopics.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = Slate700, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No encontramos lo que buscas", color = Slate500)
                    TextButton(onClick = { searchQuery = "" }) {
                        Text("Ver todos los temas", color = Emerald400, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Grid logic
            val rowItems = mutableListOf<List<Topic>>()
            var i = 0
            while (i < filteredTopics.size) {
                if (expandedTopicId == filteredTopics[i].id) {
                    rowItems.add(listOf(filteredTopics[i]))
                    i += 1
                } else if (i + 1 < filteredTopics.size && expandedTopicId != filteredTopics[i+1].id) {
                    rowItems.add(listOf(filteredTopics[i], filteredTopics[i+1]))
                    i += 2
                } else {
                    rowItems.add(listOf(filteredTopics[i]))
                    i += 1
                }
            }

            items(rowItems) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (topic in row) {
                        val isExpanded = expandedTopicId == topic.id
                        ElevatedCard(
                            modifier = Modifier
                                .weight(if (isExpanded) 1f else 0.5f),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = Slate800),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedTopicId = if (isExpanded) null else topic.id }
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(topic.color.copy(alpha = 0.15f), Transparent)
                                            )
                                        )
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(44.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                color = Slate900.copy(alpha = 0.6f),
                                                border = BorderStroke(1.dp, Slate700)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(topic.icon, contentDescription = null, tint = topic.color, modifier = Modifier.size(24.dp))
                                                }
                                            }
                                            if (isExpanded) {
                                                Icon(Icons.Default.ExpandLess, contentDescription = null, tint = Slate500)
                                            } else {
                                                Surface(
                                                    color = Slate900.copy(alpha = 0.5f),
                                                    shape = CircleShape
                                                ) {
                                                    Text(
                                                        topic.count.toString(), 
                                                        color = Slate400, 
                                                        fontSize = 12.sp, 
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(topic.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Slate900.copy(alpha = 0.3f))
                                            .padding(bottom = 12.dp)
                                    ) {
                                        topic.subtopics.forEach { sub ->
                                            Surface(
                                                color = Transparent,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        navController.navigate("feed?tag=$sub") {
                                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                            launchSingleTop = false
                                                        }
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(sub, color = Slate300, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                                    Icon(Icons.Default.ChevronRight
                                                        , contentDescription = null, tint = Slate600, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (row.size == 1 && expandedTopicId != row[0].id) {
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }
            }
        }
    }
}
