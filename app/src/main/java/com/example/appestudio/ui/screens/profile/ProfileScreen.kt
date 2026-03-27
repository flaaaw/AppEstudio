package com.example.appestudio.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.navigation.NavController
import com.example.appestudio.data.SessionManager
import com.example.appestudio.data.models.PostDto
import com.example.appestudio.data.models.UpdateUserRequest
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.navigation.Screen
import com.example.appestudio.utils.toRelativeTime
import com.example.appestudio.utils.ImageUtils
import com.example.appestudio.ui.theme.*
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, sessionManager: SessionManager? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name     by remember { mutableStateOf(sessionManager?.getName() ?: "Usuario") }
    var career   by remember { mutableStateOf(sessionManager?.getCareer() ?: "") }
    var semester by remember { mutableStateOf(sessionManager?.getSemester() ?: 1) }
    var avatarUrl by remember { mutableStateOf(sessionManager?.getAvatarUrl() ?: "") }
    val email     = sessionManager?.getEmail() ?: ""
    val userId    = sessionManager?.getUserId() ?: ""
    var isUploadingAvatar by remember { mutableStateOf(false) }

    // Avatar image picker
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch {
                isUploadingAvatar = true
                try {
                    val mime = context.contentResolver.getType(it) ?: "image/jpeg"
                    val tmp = if (mime.startsWith("image/")) {
                        ImageUtils.compressImage(context, it, "avatar_upload.jpg")
                    } else {
                        val stream = context.contentResolver.openInputStream(it)!!
                        val f = File(context.cacheDir, "avatar_upload")
                        FileOutputStream(f).use { out -> stream.copyTo(out) }
                        f
                    }
                    
                    if (tmp != null) {
                        val reqFile = tmp.asRequestBody(mime.toMediaTypeOrNull())
                        val part = MultipartBody.Part.createFormData("avatar", tmp.name, reqFile)
                        val resp = RetrofitClient.instance.uploadAvatar(userId, part)
                        if (resp.isSuccessful) {
                            val newUrl = resp.body()?.get("avatarUrl") ?: ""
                            avatarUrl = newUrl
                            sessionManager?.saveAvatarUrl(newUrl)
                        }
                    }
                } catch (_: Exception) {}
                isUploadingAvatar = false
            }
        }
    }

    var myPosts    by remember { mutableStateOf<List<PostDto>>(emptyList()) }
    var postsCount by remember { mutableStateOf(0) }
    var showEdit   by remember { mutableStateOf(false) }

    // Load my posts
    LaunchedEffect(userId) {
        if (userId.isBlank()) return@LaunchedEffect
        try {
            val resp = RetrofitClient.instance.getPostsByUser(userId)
            if (resp.isSuccessful) {
                myPosts = resp.body() ?: emptyList()
                postsCount = myPosts.size
            }
        } catch (_: Exception) {}
    }

    // Edit profile modal
    if (showEdit) {
        EditProfileModal(
            currentName = name,
            currentCareer = career,
            currentSemester = semester,
            onDismiss = { showEdit = false },
            onSave = { newName, newCareer, newSemester ->
                scope.launch {
                    try {
                        val resp = RetrofitClient.instance.updateUser(
                            userId, UpdateUserRequest(name = newName, career = newCareer, semester = newSemester)
                        )
                        if (resp.isSuccessful) {
                            name = newName; career = newCareer; semester = newSemester
                            sessionManager?.updateProfile(newName, newCareer, newSemester)
                        }
                    } catch (_: Exception) {}
                    showEdit = false
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Slate900),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────
        item {
            Box(modifier = Modifier.fillMaxWidth().background(Slate800).padding(24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Avatar — tappable, shows actual photo if uploaded
                    Box(modifier = Modifier.size(90.dp).clickable { avatarPicker.launch("image/*") }, contentAlignment = Alignment.BottomEnd) {
                        if (avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(90.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(Emerald500), contentAlignment = Alignment.Center) {
                                Text(name.take(1).uppercase(), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Slate800).border(2.dp, Slate700, CircleShape), contentAlignment = Alignment.Center) {
                            if (isUploadingAvatar) CircularProgressIndicator(color = Emerald400, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto", tint = Slate300, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("@${email.substringBefore('@')}", color = Emerald400, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                        Text("$career • Semestre $semester", color = Slate400, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    // Edit button
                    OutlinedButton(
                        onClick = { showEdit = true },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar Perfil", fontSize = 13.sp)
                    }
                }
            }
        }

        // ── Stats ────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = postsCount.toString(), label = "Publicaciones")
                StatItem(value = myPosts.sumOf { it.likes }.toString(), label = "Me gusta recibidos")
                StatItem(value = semester.toString(), label = "Semestre")
            }
        }

        // ── My Posts ─────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mis Publicaciones", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                if (myPosts.isEmpty()) Unit else Text("${myPosts.size}", color = Emerald400, fontSize = 14.sp)
            }
        }

        if (myPosts.isEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = Slate600, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aún no has publicado nada", color = Slate500, fontSize = 14.sp)
                }
            }
        } else {
            items(myPosts) { post ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp)).background(Slate800)
                        .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(16.dp)
                ) {
                    Column {
                        Text(post.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(post.content, color = Slate400, fontSize = 13.sp, maxLines = 2)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Red500, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(post.likes.toString(), color = Slate400, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                                navController.navigate("comments/${post._id}")
                            }) {
                                Icon(Icons.Default.ChatBubble, contentDescription = null, tint = Slate500, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(post.comments.toString(), color = Slate400, fontSize = 12.sp)
                            }
                            Text(post.createdAt.toRelativeTime(), color = Slate600, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ── Logout ───────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    sessionManager?.clearSession()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red500.copy(alpha = 0.15f))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Red500, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", color = Red500, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Slate400, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileModal(
    currentName: String,
    currentCareer: String,
    currentSemester: Int,
    onDismiss: () -> Unit,
    onSave: (name: String, career: String, semester: Int) -> Unit
) {
    var name     by remember { mutableStateOf(currentName) }
    var career   by remember { mutableStateOf(currentCareer) }
    var semester by remember { mutableStateOf(currentSemester.toString()) }
    var isSaving by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Slate900, modifier = Modifier.fillMaxHeight(0.75f)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Editar Perfil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Slate400) }
            }

            listOf(Triple("Nombre", name, { v: String -> name = v }),
                   Triple("Carrera", career, { v: String -> career = v })).forEach { (label, value, onChange) ->
                Text(label, color = Slate400, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = value, onValueChange = onChange,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald500, unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedContainerColor = Slate800, unfocusedContainerColor = Slate800
                    ),
                    singleLine = true
                )
            }

            Text("Semestre", color = Slate400, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
            OutlinedTextField(
                value = semester, onValueChange = { semester = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500, unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800, unfocusedContainerColor = Slate800
                ),
                singleLine = true
            )

            Button(
                onClick = {
                    if (name.isBlank() || career.isBlank()) return@Button
                    isSaving = true
                    onSave(name.trim(), career.trim(), semester.toIntOrNull() ?: 1)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                enabled = !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
