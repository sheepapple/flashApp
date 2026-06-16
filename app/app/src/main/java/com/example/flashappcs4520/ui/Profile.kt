package com.example.flashappcs4520.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashappcs4520.common.AVAILABLE_TOPICS
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    userName: String = "username",
    interests: List<String> = emptyList(),
    onBackClick: () -> Unit = {},
    onUsernameChange: (String) -> Unit = {},
    onTopicToggle: (String) -> Unit = {},
) {
    var showDialog by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(userName) }

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
                        text = "Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.secondaryFixed,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile picture",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(52.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            TextButton(onClick = { draft = userName; showDialog = true }) {
                Text("Change username")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Change username") },
                    text = {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            singleLine = true,
                            label = { Text("New username") },
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (draft.isNotBlank()) {
                                onUsernameChange(draft.trim())
                                showDialog = false
                            }
                        }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                    }
                )
            }

            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Areas of Interest",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AVAILABLE_TOPICS.forEach { topic ->
                        FilterChip(
                            selected = topic in interests,
                            onClick = { onTopicToggle(topic) },
                            label = { Text(topic) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    FlashAppCS4520Theme {
        ProfileScreen(
            userName = "testUser",
            interests = listOf("Technology", "Football"),
        )
    }
}