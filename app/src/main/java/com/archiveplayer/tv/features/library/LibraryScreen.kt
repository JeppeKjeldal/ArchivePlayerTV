package com.archiveplayer.tv.features.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.archiveplayer.tv.data.local.LibrarySection
import com.archiveplayer.tv.data.local.MediaItemEntity
import com.archiveplayer.tv.data.local.RecentlyWatchedItem

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = viewModel(),
    onNavigateToAddSource: () -> Unit,
    onNavigateToDirectAdd: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onItemClicked: (String) -> Unit
) {
    val profile by libraryViewModel.userProfile.collectAsState(initial = null)
    val sections by libraryViewModel.sections.collectAsState(initial = emptyList())
    val recentlyWatched by libraryViewModel.recentlyWatched.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back, ${profile?.profileName ?: "Guest"}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Continue watching or browse your library",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF)
                )
            }

            Row {
                Button(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Settings")
                }
                Button(
                    onClick = onNavigateToDirectAdd,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Direct URL")
                }
                Button(onClick = onNavigateToAddSource) {
                    Text("Add Source")
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (sections.isEmpty() && recentlyWatched.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your library is empty",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start by adding some content from Archive.org",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF9CA3AF)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onNavigateToAddSource) {
                        Text("Add Content")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                if (recentlyWatched.isNotEmpty()) {
                    item {
                        RecentlyWatchedSection(
                            items = recentlyWatched,
                            onItemClicked = onItemClicked
                        )
                    }
                }

                items(
                    items = sections.filter { it.categoryName != "Recently Watched" },
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

@Composable
private fun RecentlyWatchedSection(
    items: List<RecentlyWatchedItem>,
    onItemClicked: (String) -> Unit
) {
    Column {
        Text(
            text = "RECENTLY WATCHED",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items, key = { it.mediaItem.id }) { item ->
                RecentlyWatchedCard(
                    item = item,
                    onClick = { onItemClicked(item.mediaItem.archiveIdentifier) }
                )
            }
        }
    }
}

@Composable
private fun RecentlyWatchedCard(
    item: RecentlyWatchedItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                AsyncImage(
                    model = item.mediaItem.thumbnailUrl,
                    contentDescription = item.mediaItem.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF111827))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFF000000))
                )

                if (item.mediaItem.durationSeconds > 0) {
                    val progress = (item.mediaItem.watchProgressSeconds.toFloat() / item.mediaItem.durationSeconds.toFloat())
                        .coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .align(Alignment.BottomStart)
                            .background(Color(0xFFE50914))
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E3A8A))
                    .padding(12.dp)
            ) {
                Text(
                    text = item.mediaItem.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.seasonNumber != null && item.episodeNumber != null) {
                    Text(
                        text = "S${item.seasonNumber.toString().padStart(2, '0')}E${item.episodeNumber.toString().padStart(2, '0')}",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (item.timeRemainingSeconds > 0) {
                    val minutes = (item.timeRemainingSeconds / 60).coerceAtLeast(1)

                    Text(
                        text = "${minutes}m left",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 2.dp)
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
            text = section.categoryName.uppercase(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFF111827))
                )

                if (item.watchProgressSeconds > 0 && item.durationSeconds > 0) {
                    val progress = (item.watchProgressSeconds.toFloat() / item.durationSeconds.toFloat())
                        .coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .align(Alignment.BottomStart)
                            .background(Color(0xFFE50914))
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E3A8A))
                    .padding(12.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}