package com.archiveplayer.tv.features.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = viewModel(),
    onNavigateToAddSource: () -> Unit,
    onItemClicked: (Int) -> Unit
) {
    val profile by libraryViewModel.userProfile.collectAsState(initial = null)
    val mediaItems by libraryViewModel.allMediaItems.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
    ) {
        // Header med knapper
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Welcome, ${profile?.profileName ?: "Gæst"}!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Row {
                Button(
                    onClick = { libraryViewModel.addTestData() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Add test-data")
                }

                Button(onClick = onNavigateToAddSource) {
                    Text("Add Source")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (mediaItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your library is empty.\nPress 'Add test-data' or 'Add Source' to add something.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )
            }
        } else {
            Text(
                text = "Your library (${mediaItems.size} titles)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                mediaItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.thumbnailUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(60.dp)
                                .height(90.dp)
                                .background(Color.Gray)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}