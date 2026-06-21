package com.example.flashappcs4520.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashappcs4520.R

/**
 * Landing screen shown on launch. "Start reading" takes a logged-in user straight to
 * the feed, otherwise to account creation; the link below goes to the login screen.
 */
@Composable
fun WelcomeScreen(
    onStartReading: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Logo + title + tagline, vertically centered in the available space.
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_flash_logo),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Flash",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "The news, in a flash.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Button(
            onClick = onStartReading,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
        ) {
            Text("Start reading", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log in", fontSize = 16.sp)
        }
    }
}