package com.example.appestudio.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appestudio.data.models.LoginRequest
import com.example.appestudio.data.models.RegisterRequest
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.data.network.SocketHandler
import com.example.appestudio.navigation.Screen
import com.example.appestudio.ui.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

import com.example.appestudio.data.SessionManager

enum class WelcomeView { CHOICE, LOGIN, REGISTER }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WelcomeScreen(navController: NavController, sessionManager: SessionManager? = null) {
    var viewState by remember { mutableStateOf(WelcomeView.CHOICE) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // Background decorations (blurred circles)
        Box(
            modifier = Modifier
                .offset(x = (-40).dp, y = (-40).dp)
                .size(250.dp)
                .blur(80.dp)
                .background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .size(250.dp)
                .blur(80.dp)
                .background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = viewState != WelcomeView.CHOICE,
                    enter = fadeIn() + slideInHorizontally(initialOffsetX = { -100 }),
                    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -100 }),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    IconButton(onClick = { viewState = WelcomeView.CHOICE }) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Back",
                            tint = Slate400,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Logo",
                    tint = Emerald500,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "UPP",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "ESTUDIO COLABORATIVO",
                color = Emerald400,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Content
            AnimatedContent(
                targetState = viewState,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + slideInVertically(
                        initialOffsetY = { 50 })).togetherWith(
                        fadeOut(animationSpec = tween(300)) + slideOutVertically(
                            targetOffsetY = { -50 })
                    )
                },
                label = "WelcomeViewTransition"
            ) { targetState ->
                when (targetState) {
                    WelcomeView.CHOICE -> ChoiceView { viewState = it }
                    WelcomeView.LOGIN -> LoginView(sessionManager = sessionManager, onLogin = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    })
                    WelcomeView.REGISTER -> RegisterView(sessionManager = sessionManager, onRegister = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    })
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ChoiceView(onSelect: (WelcomeView) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = { onSelect(WelcomeView.LOGIN) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
        ) {
            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick = { onSelect(WelcomeView.REGISTER) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White, containerColor = Slate800),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear Cuenta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun LoginView(sessionManager: SessionManager? = null, onLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Local validation
    fun validate(): Boolean {
        var ok = true
        if (email.isBlank()) { emailError = "El correo es requerido"; ok = false }
        else if (!email.contains("@") || !email.contains(".")) { emailError = "Formato de correo inválido"; ok = false }
        else emailError = null
        if (password.isBlank()) { passwordError = "La contraseña es requerida"; ok = false }
        else passwordError = null
        return ok
    }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text("Iniciar Sesión", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)

        generalError?.let {
            Box(modifier = Modifier.fillMaxWidth().background(Red500.copy(alpha=0.1f), RoundedCornerShape(12.dp)).border(1.dp, Red500.copy(alpha=0.3f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                Text(it, color = Red500, fontSize = 14.sp)
            }
        }

        CustomTextField(label = "Correo Electrónico", placeholder = "usuario@correo.com", value = email,
            onValueChange = { email = it; emailError = null }, error = emailError,
            keyboardType = KeyboardType.Email)

        CustomTextField(label = "Contraseña", placeholder = "••••••••", value = password,
            onValueChange = { password = it; passwordError = null }, isPassword = true, error = passwordError)

        // Stay signed-in checkbox (session is always persisted, this is informational)
        var staySignedIn by remember { mutableStateOf(true) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { staySignedIn = !staySignedIn }
        ) {
            Checkbox(
                checked = staySignedIn,
                onCheckedChange = { staySignedIn = it },
                colors = CheckboxDefaults.colors(checkedColor = Emerald500, uncheckedColor = Slate600)
            )
            Text("Mantener sesión iniciada", color = Slate400, fontSize = 14.sp)
        }

        Button(
            onClick = {
                if (!validate()) return@Button
                scope.launch {
                    isLoading = true
                    generalError = null
                    try {
                        val response = RetrofitClient.instance.login(LoginRequest(email, password))
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                sessionManager?.saveSession(
                                    token    = body.token,
                                    name     = body.user.name,
                                    email    = body.user.email,
                                    career   = body.user.career,
                                    id       = body.user.id,
                                    semester = body.user.semester
                                )
                                SocketHandler.getSocket()?.emit("join", body.user.id)
                            }
                            onLogin()
                        } else {
                            val errJson = response.errorBody()?.string()
                            val type = object : TypeToken<Map<String, Any>>() {}.type
                            val errMap: Map<String, Any>? = Gson().fromJson(errJson, type)
                            val errors = errMap?.get("errors") as? Map<*, *>
                            emailError = errors?.get("email") as? String
                            passwordError = errors?.get("password") as? String
                            if (emailError == null && passwordError == null)
                                generalError = "Error al iniciar sesión. Intenta de nuevo."
                        }
                    } catch (e: Exception) {
                        generalError = "No se pudo conectar al servidor. Revisa tu conexión."
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun RegisterView(sessionManager: SessionManager? = null, onRegister: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Local validation
    fun validate(): Boolean {
        var ok = true
        if (name.trim().length < 2) { nameError = "El nombre debe tener al menos 2 caracteres"; ok = false } else nameError = null
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && (!email.contains("@") || !email.contains("."))) { emailError = "Formato de correo inválido"; ok = false } else emailError = null
        if (password.length < 6) { passwordError = "Mínimo 6 caracteres"; ok = false }
        else if (!password.any { it.isUpperCase() }) { passwordError = "Debe contener al menos una mayúscula"; ok = false }
        else if (!password.any { it.isDigit() }) { passwordError = "Debe contener al menos un número"; ok = false }
        else passwordError = null
        if (confirmPassword != password) { confirmError = "Las contraseñas no coinciden"; ok = false } else confirmError = null
        return ok
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text("Crear Cuenta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)

        generalError?.let {
            Box(modifier = Modifier.fillMaxWidth().background(Red500.copy(alpha=0.1f), RoundedCornerShape(12.dp)).border(1.dp, Red500.copy(alpha=0.3f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                Text(it, color = Red500, fontSize = 14.sp)
            }
        }

        CustomTextField(label = "Nombre Completo", placeholder = "Ej. Carlos Martínez", value = name,
            onValueChange = { name = it; nameError = null }, error = nameError)

        CustomTextField(label = "Correo Electrónico", placeholder = "usuario@correo.com", value = email,
            onValueChange = { email = it; emailError = null }, error = emailError,
            keyboardType = KeyboardType.Email)

        CustomTextField(label = "Contraseña", placeholder = "Mínimo 6 caracteres, 1 mayúscula, 1 número",
            value = password, onValueChange = { password = it; passwordError = null },
            isPassword = true, error = passwordError)

        CustomTextField(label = "Confirmar Contraseña", placeholder = "Repite tu contraseña",
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmError = if (password != it) "Las contraseñas no coinciden" else null },
            isPassword = true, error = confirmError)

        Button(
            onClick = {
                if (!validate()) return@Button
                scope.launch {
                    isLoading = true
                    generalError = null
                    try {
                        val response = RetrofitClient.instance.register(RegisterRequest(name, email, password))
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                sessionManager?.saveSession(
                                    token    = body.token,
                                    name     = body.user.name,
                                    email    = body.user.email,
                                    career   = body.user.career,
                                    id       = body.user.id,
                                    semester = body.user.semester
                                )
                                SocketHandler.getSocket()?.emit("join", body.user.id)
                            }
                            onRegister()
                        } else {
                            val errJson = response.errorBody()?.string()
                            val type = object : TypeToken<Map<String, Any>>() {}.type
                            val errMap: Map<String, Any>? = Gson().fromJson(errJson, type)
                            val errors = errMap?.get("errors") as? Map<*, *>
                            nameError = errors?.get("name") as? String
                            emailError = errors?.get("email") as? String
                            passwordError = errors?.get("password") as? String
                            if (nameError == null && emailError == null && passwordError == null)
                                generalError = "No se pudo crear la cuenta. Intenta de nuevo."
                        }
                    } catch (e: Exception) {
                        generalError = "No se pudo conectar al servidor. Revisa tu conexión."
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    suffix: String? = null,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            color = Slate400,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        val borderColor = if (error != null) Red500 else Slate700
        val focusedBorderColor = if (error != null) Red500 else Emerald500

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Slate500) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate800.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = borderColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Emerald500,
                errorBorderColor = Red500
            ),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            trailingIcon = {
                if (error != null) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Red500)
                } else if (value.isNotEmpty() && suffix == null && !isPassword) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = Emerald500)
                } else if (suffix != null) {
                    Text(text = suffix, color = Slate500, modifier = Modifier.padding(end = 16.dp))
                }
            },
            isError = error != null
        )

        AnimatedVisibility(visible = error != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Red500, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = error ?: "", color = Red500, fontSize = 12.sp)
            }
        }
    }
}
