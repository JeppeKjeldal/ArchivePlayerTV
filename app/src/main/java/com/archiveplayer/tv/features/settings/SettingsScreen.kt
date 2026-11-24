package com.archiveplayer.tv.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.archiveplayer.tv.ArchivePlayerApplication

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ArchivePlayerApplication

    var apiKeyInput by remember { mutableStateOf(app.settingsManager.tmdbApiKey ?: "") }
    var isSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(48.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { onBackClick() }
                .focusable()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Back to Library",
                color = Color(0xFF9CA3AF),
                fontSize = 16.sp
            )
        }

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "TMDB API Key",
            fontSize = 20.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Insert your personal TMDB API-key to get movie/show-metadata. You can get one for free at https://www.themoviedb.org/settings/api",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = apiKeyInput,
            onValueChange = {
                apiKeyInput = it
                isSaved = false
            },
            label = { Text("TMDB API Key") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                app.settingsManager.tmdbApiKey = apiKeyInput.trim()
                isSaved = true
            }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    app.settingsManager.tmdbApiKey = apiKeyInput.trim()
                    isSaved = true
                }
            ) {
                Text("Save")
            }

            Button(
                onClick = {
                    app.settingsManager.clearApiKey()
                    apiKeyInput = ""
                    isSaved = false
                }
            ) {
                Text("Clear")
            }
        }

        if (isSaved) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✓ API key saved successfully",
                color = Color(0xFF4ADE80),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Status",
            fontSize = 20.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        val hasKey = app.settingsManager.hasApiKey()
        Text(
            text = if (hasKey) "✓ TMDB API key is configured" else "✗ No TMDB API key configured",
            color = if (hasKey) Color(0xFF4ADE80) else Color(0xFFEF4444),
            fontSize = 14.sp
        )
    }
}