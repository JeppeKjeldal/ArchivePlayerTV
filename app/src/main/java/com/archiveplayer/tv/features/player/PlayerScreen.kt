package com.archiveplayer.tv.features.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.archiveplayer.tv.data.local.MediaItemEntity

@Composable
fun PlayerScreen(
    mediaItem: MediaItemEntity,
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current

    val episodes by viewModel.episodes.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val currentFileName by viewModel.currentEpisodeFileName.collectAsState()
    val currentTitle by viewModel.title.collectAsState()
    val currentDescription by viewModel.description.collectAsState()

    LaunchedEffect(mediaItem.archiveIdentifier) {
        viewModel.loadMedia(identifier = mediaItem.archiveIdentifier)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 48.dp, vertical = 24.dp)
    ) {
        // Back Button
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

        // Video Player Container
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = viewModel.exoPlayer
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
                .border(1.dp, Color(0xFF1F2937))
        )

        // Video Info Section
        Column(modifier = Modifier.padding(top = 24.dp)) {
            Text(
                text = currentTitle,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            val currentEpisode = episodes.find { it.fileName == currentFileName }
            if (currentEpisode != null && episodes.size > 1) {
                Text(
                    text = "Season ${currentEpisode.seasonNumber}, Episode ${currentEpisode.episodeNumber}",
                    fontSize = 18.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }

            val displayDescription = currentEpisode?.overview?.takeIf { it.isNotBlank() }
                ?: currentDescription
                ?: "No description available."

            Text(
                text = displayDescription,
                fontSize = 16.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Episode Selector
        if (episodes.size > 1) {
            EpisodeSelectorSection(
                episodes = episodes,
                selectedSeason = selectedSeason,
                currentFileName = currentFileName,
                onSeasonSelected = { viewModel.selectSeason(it) },
                onEpisodeSelected = { viewModel.playEpisode(it, mediaItem.archiveIdentifier) }
            )
        }
    }
}

@Composable
private fun EpisodeSelectorSection(
    episodes: List<Episode>,
    selectedSeason: Int,
    currentFileName: String,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeSelected: (String) -> Unit
) {
    val seasons = episodes.map { it.seasonNumber }.distinct().sorted()
    val filteredEpisodes = episodes.filter { it.seasonNumber == selectedSeason }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .background(Color(0xFF111827))
            .border(1.dp, Color(0xFF1F2937))
            .padding(24.dp)
    ) {
        Text(
            text = "Episodes",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Season Selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            seasons.forEach { season ->
                val isSelected = season == selectedSeason
                Text(
                    text = "Season $season",
                    modifier = Modifier
                        .background(if (isSelected) Color(0xFFE50914) else Color(0xFF374151))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFFE50914) else Color(0xFF4B5563)
                        )
                        .clickable { onSeasonSelected(season) }
                        .focusable()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // Horizontal Episode List
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredEpisodes) { episode ->
                EpisodeCard(
                    episode = episode,
                    isActive = episode.fileName == currentFileName,
                    onClick = { onEpisodeSelected(episode.fileName) }
                )
            }
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: Episode,
    isActive: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isActive || isFocused) Color(0xFFE50914) else Color(0xFF374151)
    val borderWidth = if (isActive || isFocused) 2.dp else 1.dp

    Column(
        modifier = Modifier
            .width(256.dp)
            .background(Color(0xFF1F2937))
            .border(borderWidth, borderColor)
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
    ) {
        if (episode.thumbnailUrl != null) {
            AsyncImage(
                model = episode.thumbnailUrl,
                contentDescription = episode.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF111827)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "▶", color = Color(0xFF4B5563), fontSize = 48.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111827))
                .padding(12.dp)
        ) {
            Text(
                text = episode.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "S${episode.seasonNumber.toString().padStart(2, '0')}E${episode.episodeNumber.toString().padStart(2, '0')}",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}