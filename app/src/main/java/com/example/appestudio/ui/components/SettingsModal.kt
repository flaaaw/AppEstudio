package com.example.appestudio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appestudio.ui.theme.Emerald500
import com.example.appestudio.ui.theme.Slate400
import com.example.appestudio.ui.theme.Slate700
import com.example.appestudio.ui.theme.Slate800
import com.example.appestudio.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModal(
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Slate900,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Configuración", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                }
            }

            // Account
            Text("Cuenta", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate800)
            ) {
                SettingsItem(Icons.Default.Person, "Editar Perfil", "Nombre, foto, biografía")
                Divider(color = Slate700.copy(alpha=0.5f))
                SettingsItem(Icons.Default.Email, "Correo Electrónico", "carlos.martinez@upp.edu.mx")
                Divider(color = Slate700.copy(alpha=0.5f))
                SettingsItem(Icons.Default.Lock, "Cambiar Contraseña", "Actualizar tu contraseña")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preferences
            Text("Preferencias", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate800)
            ) {
                var darkMode by remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { darkMode = !darkMode }.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = Slate400)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Modo Oscuro", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Switch(checked = darkMode, onCheckedChange = { darkMode = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Emerald500, uncheckedTrackColor = Slate700))
                }
                Divider(color = Slate700.copy(alpha=0.5f))
                SettingsItem(Icons.Default.Language, "Idioma", "Español")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy
            Text("Privacidad y Seguridad", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate800)
            ) {
                SettingsItem(Icons.Default.Shield, "Privacidad de Cuenta", "Controla quién ve tu perfil")
                Divider(color = Slate700.copy(alpha=0.5f))
                SettingsItem(Icons.Default.PieChart, "Datos y Almacenamiento", "Gestionar datos de la app")
            }
        }
    }
}

@Composable
fun SettingsItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Slate400, fontSize = 12.sp)
        }
    }
}
