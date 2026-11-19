package com.archiveplayer.tv.features.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Velkommen, ${profile?.profileName ?: "Gæst"}!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Button(onClick = onNavigateToAddSource) {
                Text("Add Source")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (mediaItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Dit bibliotek er tomt.\nTryk på 'Add Source' for at tilføje noget.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )
            }
        } else {
            Text(
                text = "Dit bibliotek (${mediaItems.size} titler)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                mediaItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray)
                            .padding(16.dp)
                    ) {
                        Text(text = item.title, color = Color.White)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = item.categoryName, color = Color.LightGray)
                    }
                }
            }
        }
    }
}