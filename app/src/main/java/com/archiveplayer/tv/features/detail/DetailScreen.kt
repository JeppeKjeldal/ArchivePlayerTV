package com.archiveplayer.tv.features.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.archiveplayer.tv.data.archive.ArchiveDetails
import com.archiveplayer.tv.data.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    itemId: String,
    onBackClick: () -> Unit
) {
    var details by remember { mutableStateOf<ArchiveDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(itemId) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.archiveApi.getMetadata(itemId)
                if (response.isSuccessful) {
                    val metadata = response.body() ?: emptyMap()
                    details = ArchiveDetails.fromMetadata(itemId, metadata)
                } else {
                    errorMessage = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Connection error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Go Back")
                        }
                    }
                }
            }
            details != null -> {
                val item = details!!

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header with back button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onBackClick) {
                            Text("← Back")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Content area
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Left side: Thumbnail placeholder
                        Box(
                            modifier = Modifier
                                .width(300.dp)
                                .height(450.dp)
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "[Thumbnail]",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        // Right side: Description and actions
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            if (item.description != null) {
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.LightGray,
                                    modifier = Modifier.heightIn(max = 300.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action buttons
                            Row {
                                Button(
                                    onClick = { /* TODO: Add to library */ },
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Text("Tilføj til bibliotek")
                                }

                                if (item.videoFiles.isNotEmpty()) {
                                    Button(
                                        onClick = { /* TODO: Play video */ }
                                    ) {
                                        Text("Afspil")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (item.videoFiles.isNotEmpty()) {
                                Text(
                                    text = "Tilgængelige filer: ${item.videoFiles.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}