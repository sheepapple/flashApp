package com.example.flashappcs4520

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashappcs4520.data.AuthRepository
import com.example.flashappcs4520.ui.ArticleViewModel
import com.example.flashappcs4520.ui.FeedScreen
import com.example.flashappcs4520.ui.LoginScreen
import com.example.flashappcs4520.ui.ProfileScreen
import com.example.flashappcs4520.ui.ProfileViewModel
import com.example.flashappcs4520.ui.SignUpScreen
import com.example.flashappcs4520.ui.WelcomeScreen
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme
import com.example.flashappcs4520.ui.SettingScreen
import com.example.flashappcs4520.ui.SettingViewModel
import com.example.flashappcs4520.ui.NotificationsScreen
import com.example.flashappcs4520.ui.NotificationViewModel
import com.example.flashappcs4520.ui.SavedScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashAppCS4520Theme {
                val auth = remember { AuthRepository() }
                val scope = rememberCoroutineScope()
                var screen by remember { mutableStateOf("welcome") }

                when (screen) {
                    "welcome" -> WelcomeScreen(
                        // Logged-in users jump straight to the feed; others create an account.
                        onStartReading = {
                            screen = if (auth.currentUser() != null) "feed" else "signup"
                        },
                        onNavigateToLogin = { screen = "login" },
                    )
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
                        val notificationViewModel: NotificationViewModel = viewModel()
                        val hasUnread by notificationViewModel.hasUnread.collectAsStateWithLifecycle()
                        FeedScreen(
                            articleViewModel = articleViewModel,
                            avatarUrl = user?.avatarUrl,
                            hasUnread = hasUnread,
                            onProfileClick = { screen = "profile" },
                            onNotificationsClick = { screen = "notifications" },
                            // Sign out and return to the welcome (home) screen.
                            onLogoutClick = {
                                scope.launch { auth.signOut() }
                                screen = "welcome"
                            },
                        )
                    }
                    "profile" -> {
                        val profileViewModel: ProfileViewModel = viewModel()
                        val user by profileViewModel.user.collectAsStateWithLifecycle()
                        val notificationViewModel: NotificationViewModel = viewModel()
                        val hasUnread by notificationViewModel.hasUnread.collectAsStateWithLifecycle()
                        ProfileScreen(
                            userName = user?.username ?: "Loading…",
                            avatarUrl = user?.avatarUrl,
                            interests = user?.interests ?: emptyList(),
                            hasUnread = hasUnread,
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
                    "notifications" -> {
                        val notificationViewModel: NotificationViewModel = viewModel()
                        // Same activity-scoped instance the feed renders, so a tapped
                        // notification can prepend its article to that feed.
                        val articleViewModel: ArticleViewModel = viewModel()
                        NotificationsScreen(
                            onBackClick = { screen = "profile" },
                            onNotificationClick = { notification ->
                                notification.articleId?.let { id ->
                                    articleViewModel.prependArticleToFeed(id)
                                    screen = "feed"
                                }
                            },
                            viewModel = notificationViewModel,
                        )
                    }
                    "saved" -> SavedScreen(onBackClick = { screen = "profile" })
                    "comments" -> {}
                }
            }
        }
    }
}