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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appestudio.data.SessionManager
import com.example.appestudio.navigation.Screen
import com.example.appestudio.ui.theme.*

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

    val filteredTopics = topics.filter {
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
            // Header with Gradient
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Slate800.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Bienvenido de vuelta,", color = Slate400, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("¡Hola, $userName! \uD83D\uDC4B", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Slate800)
                                .clickable { showNotifications = !showNotifications },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Slate300, modifier = Modifier.size(20.dp))
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Emerald500)
                                    .border(1.dp, Slate800, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar temas, ejercicios...", color = Slate500) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate800, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }
        }

        item {
            // Quick Access
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text("Accesos Rápidos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val quickAccessItems = listOf("Ejercicios Recientes", "Material Nuevo", "Tutorías Grabadas")
                    items(quickAccessItems.size) { index ->
                        val text = quickAccessItems[index]
                        val tint = when (index) {
                            0 -> Blue500
                            1 -> Emerald500
                            else -> Violet500
                        }
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(96.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Slate800)
                                .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .clickable {
                                    if (text == "Tutorías Grabadas") navController.navigate(Screen.Videos.route)
                                    else navController.navigate(Screen.Feed.route)
                                }
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(tint.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                                }
                                Text(text, color = Slate200, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Explorar Temas", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp))
        }

        if (filteredTopics.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No se encontraron temas", color = Slate400)
                    TextButton(onClick = { searchQuery = "" }) {
                        Text("Limpiar búsqueda", color = Emerald400)
                    }
                }
            }
        } else {
            // Because we want to simulate a grid of 2 columns with spanning
            val rowItems = mutableListOf<List<Topic>>()
            var i = 0
            while (i < filteredTopics.size) {
                if (expandedTopicId == filteredTopics[i].id) {
                    // Takes full width
                    rowItems.add(listOf(filteredTopics[i]))
                    i += 1
                } else if (i + 1 < filteredTopics.size && expandedTopicId != filteredTopics[i+1].id) {
                    // Make a row of 2
                    rowItems.add(listOf(filteredTopics[i], filteredTopics[i+1]))
                    i += 2
                } else {
                    // Last item alone
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
                        Box(
                            modifier = Modifier
                                .weight(if (isExpanded) 1f else 0.5f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Slate800)
                                .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedTopicId = if (isExpanded) null else topic.id
                                        }
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    topic.color.copy(alpha = 0.2f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                        .padding(16.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Slate900.copy(alpha = 0.5f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(topic.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                            if (isExpanded) {
                                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Slate400)
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Slate900.copy(alpha = 0.5f), CircleShape)
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(topic.count.toString(), color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(topic.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Slate800.copy(alpha = 0.5f))
                                            .padding(16.dp)
                                    ) {
                                        topic.subtopics.forEach { sub ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        navController.navigate("feed?tag=$sub")
                                                    }
                                                    .padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(sub, color = Slate300, fontSize = 14.sp)
                                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate600, modifier = Modifier.size(16.dp))
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
val Slate200 = Color(0xFFE2E8F0)
