package com.example.frontend.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.frontend.net.ApiClient
import com.example.frontend.net.TokenStore
import com.example.frontend.net.dto.LoginRequestDto
import com.example.frontend.net.dto.UserCreateDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AuthScreen(onLoggedIn: () -> Unit) {
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regFull by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPass by remember { mutableStateOf("") }

    var loginUser by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }

    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        //Registration
        Text("Register", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = regUsername, onValueChange = { regUsername = it }, label = { Text("Username") })
        OutlinedTextField(value = regEmail, onValueChange = { regEmail = it }, label = { Text("Email") })
        OutlinedTextField(value = regFull, onValueChange = { regFull = it }, label = { Text("Full Name") })
        OutlinedTextField(value = regPhone, onValueChange = { regPhone = it }, label = { Text("Phone Number") })
        OutlinedTextField(
            value = regPass,
            onValueChange = { regPass = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        ApiClient.api.register(UserCreateDto(regUsername, regEmail, regPass, regFull, regPhone))
                    }
                    status = "Registered ✅"
                } catch (e: Exception) {
                    status = "Register failed: ${e.message}"
                }
            }
        }) {
            Text("Register")
        }

        Spacer(Modifier.height(24.dp))

        //login
        Text("Login", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = loginUser, onValueChange = { loginUser = it }, label = { Text("Username or Email") })
        OutlinedTextField(
            value = loginPass,
            onValueChange = { loginPass = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            scope.launch {
                try {
                    val res = withContext(Dispatchers.IO) {
                        ApiClient.api.login(LoginRequestDto(loginUser, loginPass))
                    }
                    TokenStore.token = res.accessToken
                    TokenStore.userId = res.userId
                    TokenStore.username = res.username
                    TokenStore.role = res.role
                    status = "Logged in as ${res.username} (${res.role})"
                    onLoggedIn() // runs on Main thread, safe
                } catch (e: Exception) {
                    status = "Login failed: ${e.message}"
                }
            }
        }) {
            Text("Login")
        }

        Spacer(Modifier.height(16.dp))
        Text(status)
    }
}
