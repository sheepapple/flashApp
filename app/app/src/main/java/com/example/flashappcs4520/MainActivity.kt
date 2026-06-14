package com.example.flashappcs4520

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashappcs4520.data.SupabaseProvider
import com.example.flashappcs4520.ui.ArticleViewModel
import com.example.flashappcs4520.ui.FeedScreen
import com.example.flashappcs4520.ui.LoginScreen
import com.example.flashappcs4520.ui.SignUpScreen
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme
import io.github.jan.supabase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashAppCS4520Theme {
                var screen by remember { mutableStateOf(
                    if (SupabaseProvider.client.auth.currentUserOrNull() != null) "feed"
                    else "login"
                )}

                when (screen) {
                    "login" -> LoginScreen(
                        onLoginSuccess = { screen = "feed" },
                        onNavigateToSignUp = { screen = "signup" }
                    )
                    "signup" -> SignUpScreen(
                        onSignUpSuccess = { screen = "feed" },
                        onNavigateToLogin = { screen = "login" }
                    )
                    "feed" -> {
                        val articleViewModel: ArticleViewModel = viewModel()
                        FeedScreen(articleViewModel = articleViewModel)
                    }
                }
            }
        }
    }
}