package com.example.wdal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wdal.ui.theme.WdALTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://pbtfuekbhffkxoyclepe.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBidGZ1ZWtiaGZma3hveWNsZXBlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU0ODgxODUsImV4cCI6MjA4MTA2NDE4NX0.DEgWLuhNqS61H0pnRf-rGk4JbpLg0UetFxkw3Uy1mk8"
) {
    install(Postgrest)
    install(Auth)
}

@Serializable
data class LoginData(
    val id: Int? = null,
    val name: String,
    val familki: String,
    val email: String,
)

class AuthViewModel {
    companion object {
        var isLoggedIn = mutableStateOf(false)
        var currentUserEmail = mutableStateOf("")

        suspend fun checkSession() {
            try {
                val session = supabase.auth.currentSessionOrNull()
                isLoggedIn.value = session != null
                if (session != null) {
                    currentUserEmail.value = session.user?.email ?: ""
                }
            } catch (e: Exception) {
                isLoggedIn.value = false
            }
        }
        suspend fun logout() {
            try {
                supabase.auth.signOut()
                isLoggedIn.value = false
                currentUserEmail.value = ""
            } catch (e: Exception) {
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WdALTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WorldCinemaApp()
                }
            }
        }
    }
}

@Composable
fun WorldCinemaApp() {
    val navController = rememberNavController()
    var isLoading by remember { mutableStateOf(true) }

    // Проверяем сессию при запуске
    LaunchedEffect(Unit) {
        AuthViewModel.checkSession()
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF332973)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFEF3A01))
        }
    } else {
        NavHost(navController = navController, startDestination = if (AuthViewModel.isLoggedIn.value) "main" else "login") {
            composable("login") {
                LoginScreen(navController)
            }
            composable("registration") {
                RegistrationScreen(navController)
            }
            composable("main") {
                MainScreen(navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF332973))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Image(
                painter = painterResource(R.drawable.wd),
                contentDescription = "Логотип",
                modifier = Modifier.size(190.dp)
            )

            // Показываем ошибку, если есть
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = userEmail,
                onValueChange = {
                    userEmail = it
                    errorMessage = null
                },
                label = {
                    Text(
                        "Email",
                        color = Color(0xFF808080)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA8A8A8),
                    unfocusedBorderColor = Color(0xFFA8A8A8),
                    cursorColor = Color(0xFFA8A8A8),
                    unfocusedTextColor = Color(0xFF808080),
                    focusedTextColor = Color(0xFF808080),
                ),
                shape = RoundedCornerShape(5.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userPassword,
                onValueChange = {
                    userPassword = it
                    errorMessage = null
                },
                label = {
                    Text(
                        "Пароль",
                        color = Color(0xFF808080)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA8A8A8),
                    unfocusedBorderColor = Color(0xFFA8A8A8),
                    cursorColor = Color(0xFFA8A8A8),
                    unfocusedTextColor = Color(0xFF808080),
                    focusedTextColor = Color(0xFF808080),
                ),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(5.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        if (isLoading) Color(0xFFEF3A01).copy(alpha = 0.7f)
                        else Color(0xFFEF3A01),
                        RoundedCornerShape(5.dp)
                    )
                    .clickable(enabled = !isLoading && userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
                        if (userEmail.isEmpty() || userPassword.isEmpty()) {
                            errorMessage = "Заполните все поля"
                            return@clickable
                        }

                        isLoading = true
                        errorMessage = null
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val result = supabase.auth.signInWith(Email) {
                                    email = userEmail
                                    password = userPassword
                                }

                                launch(Dispatchers.Main) {
                                    AuthViewModel.isLoggedIn.value = true
                                    AuthViewModel.currentUserEmail.value = userEmail
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Ошибка входа: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).align(Alignment.Center),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Войти",
                        color = Color(0xFFFFFFFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Transparent)
                    .border(1.dp, Color(0xFFA8A8A8), RoundedCornerShape(5.dp))
                    .clickable { navController.navigate("registration") }
            ) {
                Text(
                    text = "Регистрация",
                    color = Color(0xFFEF3A01),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(navController: NavHostController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF332973))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Image(
                painter = painterResource(R.drawable.wd),
                contentDescription = "Логотип",
                modifier = Modifier.size(190.dp)
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f, fill = false),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    errorMessage = null
                },
                label = {
                    Text(
                        "Имя",
                        color = Color(0xFF808080)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA8A8A8),
                    unfocusedBorderColor = Color(0xFFA8A8A8),
                    cursorColor = Color(0xFFA8A8A8),
                    unfocusedTextColor = Color(0xFF808080),
                    focusedTextColor = Color(0xFF808080),
                ),
                shape = RoundedCornerShape(5.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    errorMessage = null
                },
                label = {
                    Text(
                        "Фамилия",
                        color = Color(0xFF808080)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA8A8A8),
                    unfocusedBorderColor = Color(0xFFA8A8A8),
                    cursorColor = Color(0xFFA8A8A8),
                    unfocusedTextColor = Color(0xFF808080),
                    focusedTextColor = Color(0xFF808080),
                ),
                shape = RoundedCornerShape(5.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = userEmail,
                onValueChange = {
                    userEmail = it
                    errorMessage = null
                },
                label = {
                    Text(
                        "Email",
                        color = Color(0xFF808080)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA8A8A8),
                    unfocusedBorderColor = Color(0xFFA8A8A8),
                    cursorColor = Color(0xFFA8A8A8),
                    unfocusedTextColor = Color(0xFF808080),
                    focusedTextColor = Color(0xFF808080),
                ),
                shape = RoundedCornerShape(5.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = userPassword,
                onValueChange = {
                    userPassword = it
                    errorMessage = null
                },
                label = {
                    Text(
                        "Пароль",
                        color = Color(0xFF808080)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA8A8A8),
                    unfocusedBorderColor = Color(0xFFA8A8A8),
                    cursorColor = Color(0xFFA8A8A8),
                    unfocusedTextColor = Color(0xFF808080),
                    focusedTextColor = Color(0xFF808080),
                ),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(5.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = {
                    Text(
                        "Повторите пароль",
                        color = Color(0xFF808080)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA8A8A8),
                    unfocusedBorderColor = Color(0xFFA8A8A8),
                    cursorColor = Color(0xFFA8A8A8),
                    unfocusedTextColor = Color(0xFF808080),
                    focusedTextColor = Color(0xFF808080),
                ),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(5.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        if (isLoading) Color(0xFFEF3A01).copy(alpha = 0.7f)
                        else Color(0xFFEF3A01),
                        RoundedCornerShape(5.dp)
                    )
                    .clickable(enabled = !isLoading) {
                        if (firstName.isEmpty() || lastName.isEmpty() ||
                            userEmail.isEmpty() || userPassword.isEmpty() ||
                            confirmPassword.isEmpty()) {
                            errorMessage = "Заполните все поля"
                            return@clickable
                        }

                        if (!userEmail.contains("@")) {
                            errorMessage = "Введите корректный email"
                            return@clickable
                        }

                        if (userPassword.length < 6) {
                            errorMessage = "Пароль должен содержать не менее 6 символов"
                            return@clickable
                        }

                        if (userPassword != confirmPassword) {
                            errorMessage = "Пароли не совпадают"
                            return@clickable
                        }

                        isLoading = true
                        errorMessage = null
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                supabase.auth.signUpWith(Email) {
                                    email = userEmail
                                    password = userPassword
                                }

                                val newUser = LoginData(
                                    name = firstName,
                                    familki = lastName,
                                    email = userEmail
                                )

                                try {
                                    supabase.from("logins")
                                        .insert(newUser)
                                } catch (e: Exception) {
                                }

                                launch(Dispatchers.Main) {
                                    navController.navigate("login") {
                                        popUpTo("registration") { inclusive = true }
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Ошибка регистрации: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).align(Alignment.Center),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Зарегистрироваться",
                        color = Color(0xFFFFFFFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Transparent)
                    .border(1.dp, Color(0xFFA8A8A8), RoundedCornerShape(5.dp))
                    .clickable { navController.popBackStack() }
            ) {
                Text(
                    text = "У меня уже есть аккаунт",
                    color = Color(0xFFEF3A01),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("В тренде", "Новое", "Для вас")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF332973))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF332973))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        AuthViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            ) {
                Text(
                    text = "Выйти",
                    color = Color(0xFFEF3A01),
                    fontSize = 16.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.main),
                contentDescription = "Главная картинка",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painterResource(R.drawable.sha),
                contentDescription = "Тень",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painterResource(R.drawable.mag),
                contentDescription = "Надпись",
                modifier = Modifier
                    .height(115.dp)
                    .width(255.dp)
                    .align(Alignment.Center)
                    .offset(y = (-70).dp)
            )

            Button(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .width(134.dp)
                    .height(44.dp)
                    .align(Alignment.Center)
                    .offset(y = 130.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF3A01)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Смотреть",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFC4C4C4).copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 13.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    selectedTab = index
                                }
                                .weight(1f)
                        ) {
                            Text(
                                text = tab,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF3A01),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(13.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        tabs.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                if (selectedTab == index) {
                                    Box(
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(5.dp)
                                            .background(Color(0xFFEF3A01))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            items(4) { index ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(144.dp)
                ) {
                    Image(
                        painter = painterResource(
                            when(index) {
                                0 -> R.drawable.mov1
                                1 -> R.drawable.mov2
                                2 -> R.drawable.mov3
                                else -> R.drawable.mov4
                            }
                        ),
                        contentDescription = "Фильм ${index + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}