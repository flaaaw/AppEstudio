package com.example.appestudio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appestudio.navigation.Screen
import com.example.appestudio.ui.theme.*

data class NavItem(
    val route: String,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)

val bottomNavItems = listOf(
    NavItem(Screen.Dashboard.route, "Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    NavItem(Screen.Chat.route, "Chat", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
    NavItem(Screen.Feed.route, "Comunidad", Icons.Outlined.PeopleOutline, Icons.Filled.People),
    NavItem(Screen.Videos.route, "Videos", Icons.Outlined.PlayCircleOutline, Icons.Filled.PlayCircleFilled),
    NavItem(Screen.Profile.route, "Perfil", Icons.Outlined.PersonOutline, Icons.Filled.Person)
)

@Composable
fun BottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // Only show bottom nav if we are not in Welcome screen
    if (currentRoute == Screen.Welcome.route) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
            .shadow(24.dp, CircleShape)
            .clip(CircleShape)
            .background(Slate800.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            onClick = { onNavigate(item.route) }
                        )
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (isSelected) Emerald500 else Slate400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) Emerald500 else Slate400
                    )
                }
            }
        }
    }
}
