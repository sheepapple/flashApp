package com.example.flashappcs4520.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    currentUsername: String = "",
    currentAvatarUrl: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: SettingViewModel = viewModel(),
) {
    val usernameSaveState by viewModel.usernameSaveState.collectAsStateWithLifecycle()
    val passwordSaveState by viewModel.passwordSaveState.collectAsStateWithLifecycle()
    val avatarSaveState by viewModel.avatarSaveState.collectAsStateWithLifecycle()

    var usernameField by remember(currentUsername) { mutableStateOf(currentUsername) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var avatarUrlField by remember(currentAvatarUrl) { mutableStateOf(currentAvatarUrl ?: "") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(usernameSaveState) {
        when (val s = usernameSaveState) {
            is SaveState.Success -> {
                snackbarHostState.showSnackbar("Username updated!")
                viewModel.resetUsernameSaveState()
            }
            is SaveState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetUsernameSaveState()
            }
            else -> Unit
        }
    }
    LaunchedEffect(passwordSaveState) {
        when (val s = passwordSaveState) {
            is SaveState.Success -> {
                snackbarHostState.showSnackbar("Password updated!")
                newPassword = ""
                confirmPassword = ""
                viewModel.resetPasswordSaveState()
            }
            is SaveState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetPasswordSaveState()
            }
            else -> Unit
        }
    }
    LaunchedEffect(avatarSaveState) {
        when (val s = avatarSaveState) {
            is SaveState.Success -> {
                snackbarHostState.showSnackbar("Profile picture updated!")
                viewModel.resetAvatarSaveState()
            }
            is SaveState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetAvatarSaveState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryFixed,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // changing username
            SettingsSection(title = "Change Username") {
                OutlinedTextField(
                    value = usernameField,
                    onValueChange = { usernameField = it },
                    label = { Text("New username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(8.dp))
                SaveButton(
                    label = "Save username",
                    state = usernameSaveState,
                    onClick = { viewModel.saveUsername(usernameField) },
                )
            }

            HorizontalDivider()

            // changing password
            SettingsSection(title = "Change Password") {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm new password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    // check for match of two new password
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                SaveButton(
                    label = "Save password",
                    state = passwordSaveState,
                    onClick = { viewModel.savePassword(newPassword, confirmPassword) },
                )
            }

            HorizontalDivider()

            // setting/changing the user profile picture url
            SettingsSection(title = "Profile Picture") {
                if (avatarUrlField.isNotBlank()) {
                    AsyncImage(
                        model = avatarUrlField,
                        contentDescription = "Profile picture preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally),
                    )
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = avatarUrlField,
                    onValueChange = { avatarUrlField = it },
                    label = { Text("Profile picture URL") },
                    placeholder = { Text("https://example.com/photo.jpg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(8.dp))
                SaveButton(
                    label = "Save picture",
                    state = avatarSaveState,
                    onClick = { viewModel.saveAvatarUrl(avatarUrlField) },
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Column(content = content)
    }
}

@Composable
private fun SaveButton(
    label: String,
    state: SaveState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = state !is SaveState.Loading,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        when (state) {
            is SaveState.Loading -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            is SaveState.Success -> Icon(Icons.Default.Check, contentDescription = null)
            else -> Text(label)
        }
    }
}