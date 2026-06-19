package com.example.flashappcs4520

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashappcs4520.ui.ArticleViewModel
import com.example.flashappcs4520.ui.FeedScreen
import com.example.flashappcs4520.ui.LoginScreen
import com.example.flashappcs4520.ui.ProfileScreen
import com.example.flashappcs4520.ui.ProfileViewModel
import com.example.flashappcs4520.ui.SignUpScreen
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme
import com.example.flashappcs4520.ui.SettingScreen
import com.example.flashappcs4520.ui.SettingViewModel

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
                        val profileViewModel: ProfileViewModel = viewModel()
                        val user by profileViewModel.user.collectAsStateWithLifecycle()
                        FeedScreen(
                            articleViewModel = articleViewModel,
                            avatarUrl = user?.avatarUrl,
                            onProfileClick = { screen = "profile" },
                        )
                    }
                    "profile" -> {
                        val profileViewModel: ProfileViewModel = viewModel()
                        val user by profileViewModel.user.collectAsStateWithLifecycle()
                        ProfileScreen(
                            userName = user?.username ?: "Loading…",
                            avatarUrl = user?.avatarUrl,
                            interests = user?.interests ?: emptyList(),
                            onBackClick = { screen = "feed" },
                            onSettingsClick = { screen = "settings" },
                            onNotificationsClick = { screen = "notifications" },
                            onSavedClick = { screen = "saved" },
                            onCommentsClick = { screen = "comments" },
                            onUsernameChange = { profileViewModel.updateUsername(it) },
                            onTopicToggle = { profileViewModel.toggleTopic(it) },
                        )
                    }
                    "settings" -> {
                        // renders SettingScreen with current user data
                        val profileViewModel: ProfileViewModel = viewModel()
                        val user by profileViewModel.user.collectAsStateWithLifecycle()
                        val settingsViewModel: SettingViewModel = viewModel()
                        SettingScreen(
                            currentUsername = user?.username ?: "",
                            currentAvatarUrl = user?.avatarUrl,
                            onBackClick = {
                                // Refresh profile data when returning from settings
                                profileViewModel.refresh()
                                screen = "profile"
                            },
                            viewModel = settingsViewModel,
                        )
                    }
                    "notifications" -> {} // TODO notifications screen
                    "saved" -> {}
                    "comments" -> {}
                }
            }
        }
    }
}