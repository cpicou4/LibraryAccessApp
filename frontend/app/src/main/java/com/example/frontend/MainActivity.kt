package com.example.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.frontend.ui.AuthScreen
import com.example.frontend.ui.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val nav: NavHostController = rememberNavController()
                NavHost(navController = nav, startDestination = "auth") {
                    composable("auth") {
                        AuthScreen(
                                onLoggedIn = {
                                    nav.navigate("home") { popUpTo("auth") { inclusive = true } }
                                }
                        )
                    }
                    composable("home") {
                        HomeScreen(
                                onLogout = {
                                    nav.navigate("auth") { popUpTo("home") { inclusive = true } }
                                }
                        )
                    }
                }
            }
        }
    }
}
