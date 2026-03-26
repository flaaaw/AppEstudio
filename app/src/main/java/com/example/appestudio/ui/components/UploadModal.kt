package com.example.appestudio.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
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
import com.example.appestudio.ui.theme.Slate700
import com.example.appestudio.ui.theme.Slate800
import com.example.appestudio.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadModal(
    onDismissRequest: () -> Unit,
    onSubmit: (title: String, content: String, tags: List<String>, fileUri: Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Duda") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fileUri = uri
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Slate900
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 50.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Crear Publicación", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate700,
                    cursorColor = Emerald500
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("¿Qué tienes en mente?", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate700,
                    cursorColor = Emerald500
                ),
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Duda", "Material", "Aviso").forEach { tag ->
                    val isSelected = selectedTag == tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Emerald500 else Slate800)
                            .clickable { selectedTag = tag }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(tag, color = if (isSelected) Color.White else Color.LightGray, fontSize = 14.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                    .clickable { launcher.launch("*/*") }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = Emerald500)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (fileUri != null) "Archivo seleccionado" else "Adjuntar imagen o PDF",
                    color = Color.LightGray
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (title.isNotEmpty() && content.isNotEmpty()) {
                        onSubmit(title, content, listOf(selectedTag), fileUri)
                        onDismissRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Publicar en la Comunidad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
