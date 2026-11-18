package com.archiveplayer.tv.features.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    var profileName by remember { mutableStateOf("Gæst") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }

    fun handleStart() {
        val name = profileName.trim()
        if (name.isBlank()) {
            errorMessage = "Profilnavn må ikke være tomt"
            return
        }

        errorMessage = null
        onLoginSuccess(name)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 500.dp)
                .padding(32.dp)
        ) {
            Text(
                text = "ArchivePlayer TV",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Velkommen! Vælg et profilnavn.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(48.dp))

            TextField(
                value = profileName,
                onValueChange = {
                    profileName = it
                    errorMessage = null
                },
                label = { Text("Profilnavn") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { handleStart() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { handleStart() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Afspilning")
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}