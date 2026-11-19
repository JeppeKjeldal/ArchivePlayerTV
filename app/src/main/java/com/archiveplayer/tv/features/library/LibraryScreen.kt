package com.archiveplayer.tv.features.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.archiveplayer.tv.data.local.LibrarySection
import com.archiveplayer.tv.data.local.MediaItemEntity

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = viewModel(),
    onNavigateToAddSource: () -> Unit,
    onItemClicked: (String) -> Unit
) {
    val profile by libraryViewModel.userProfile.collectAsState(initial = null)
    val sections by libraryViewModel.sections.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Welcome back, ${profile?.profileName ?: "Guest"}!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Row {
                Button(onClick = onNavigateToAddSource) {
                    Text("Add Source")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (sections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your library is empty.\nUse 'Add Source' to find something to watch.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )
            }
        } else {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(
                    items = sections,
                    key = { it.categoryName }
                ) { section ->
                    CategorySection(
                        section = section,
                        onItemClicked = onItemClicked
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(
    section: LibrarySection,
    onItemClicked: (String) -> Unit
) {
    Column {
        Text(
            text = section.categoryName,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            section.items.forEach { item ->
                MediaCard(
                    item = item,
                    onClick = { onItemClicked(item.archiveIdentifier) }
                )
            }
        }
    }
}

@Composable
private fun MediaCard(
    item: MediaItemEntity,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale = if (isFocused) 1.05f else 1f

    Column(
        modifier = Modifier
            .width(150.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = item.thumbnailUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    if (isFocused) Color.Gray else Color.DarkGray
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isFocused) Color.DarkGray else Color(0xFF1A1A2E))
                .padding(8.dp)
        ) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.categoryName,
                fontSize = 11.sp,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}