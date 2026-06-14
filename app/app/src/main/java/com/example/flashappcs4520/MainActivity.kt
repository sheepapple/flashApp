package com.example.flashappcs4520

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashappcs4520.ui.ArticleViewModel
import com.example.flashappcs4520.ui.FeedScreen
import com.example.flashappcs4520.ui.LoginScreen
import com.example.flashappcs4520.ui.ProfileScreen
import com.example.flashappcs4520.ui.SignUpScreen
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashAppCS4520Theme {
                var screen by remember { mutableStateOf("login") }

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
                        FeedScreen(
                            articleViewModel = articleViewModel,
                            onProfileClick = { screen = "profile" },  // add this
                        )
                    }
                    "profile" -> ProfileScreen(
                        userName = "username",
                        interests = listOf("Technology", "Finance", "World News"),
                        onBackClick = { screen = "feed" },
                    )
                }
            }
        }
    }
}