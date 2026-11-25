
package com.archiveplayer.tv.features.player

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.clip
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.archiveplayer.tv.data.local.MediaItemEntity
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlin.time.Duration.Companion.milliseconds

private const val CONTROLS_HIDE_DELAY_MS = 5_000L
private const val SEEK_STEP_MS = 10_000L

private val PlayerRed = Color(0xFFE50914)
private val OverlayBackground = Color(0xE6000000)

private enum class PlayerUiState {
    FULLSCREEN,
    CONTROLS,
    EPISODE_INFO,
    EPISODE_NAVIGATION
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    mediaItem: MediaItemEntity,
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val episodes by viewModel.episodes.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val currentEpisodeFileName by viewModel.currentEpisodeFileName.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()

    var uiState by remember {
        mutableStateOf(PlayerUiState.FULLSCREEN)
    }

    var playerPosition by remember {
        mutableLongStateOf(0L)
    }

    var playerDuration by remember {
        mutableLongStateOf(0L)
    }

    var interactionCounter by remember {
        mutableIntStateOf(0)
    }

    val rootFocusRequester = remember { FocusRequester() }
    val controlsFocusRequester = remember { FocusRequester() }
    val seasonFocusRequester = remember { FocusRequester() }
    val episodeFocusRequester = remember { FocusRequester() }

    val currentEpisode = remember(episodes, currentEpisodeFileName) {
        episodes.firstOrNull {
            it.fileName == currentEpisodeFileName
        }
    }

    val currentEpisodeTitle = currentEpisode
        ?.displayName
        ?.takeIf { it.isNotBlank() }
        ?: title

    val currentEpisodeDescription = currentEpisode
        ?.description
        ?.takeIf { it.isNotBlank() }
        ?: currentEpisode?.overview?.takeIf { it.isNotBlank() }
        ?: description

    val seasons = remember(episodes) {
        episodes
            .map { it.seasonNumber }
            .distinct()
            .sorted()
    }

    val seasonEpisodes = remember(episodes, selectedSeason) {
        episodes
            .filter { it.seasonNumber == selectedSeason }
            .sortedBy { it.episodeNumber }
    }

    LaunchedEffect(mediaItem.archiveIdentifier) {
        viewModel.loadMedia(mediaItem.archiveIdentifier)
    }

    LaunchedEffect(viewModel.exoPlayer) {
        while (true) {
            playerPosition = viewModel.exoPlayer.currentPosition
                .coerceAtLeast(0L)

            val duration = viewModel.exoPlayer.duration

            playerDuration = if (duration > 0L) {
                duration
            } else {
                0L
            }

            delay(500.milliseconds)
        }
    }

    LaunchedEffect(uiState) {
        delay(100.milliseconds)

        when (uiState) {
            PlayerUiState.FULLSCREEN -> {
                runCatching {
                    rootFocusRequester.requestFocus()
                }
            }

            PlayerUiState.CONTROLS -> {
                runCatching {
                    controlsFocusRequester.requestFocus()
                }
            }

            PlayerUiState.EPISODE_INFO -> {
                if (seasons.isNotEmpty()) {
                    runCatching {
                        seasonFocusRequester.requestFocus()
                    }
                }
            }

            PlayerUiState.EPISODE_NAVIGATION -> {
                if (seasonEpisodes.isNotEmpty()) {
                    runCatching {
                        episodeFocusRequester.requestFocus()
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState, interactionCounter) {
        if (uiState != PlayerUiState.FULLSCREEN) {
            delay(CONTROLS_HIDE_DELAY_MS.milliseconds)

            uiState = PlayerUiState.FULLSCREEN
        }
    }

    BackHandler {
        if (uiState != PlayerUiState.FULLSCREEN) {
            uiState = PlayerUiState.FULLSCREEN
        } else {
            onBackClick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(rootFocusRequester)
            .onPreviewKeyEvent { event ->

                if (event.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }

                interactionCounter++

                when (event.key) {

                    Key.DirectionDown -> {
                        when (uiState) {

                            PlayerUiState.FULLSCREEN -> {
                                uiState = PlayerUiState.CONTROLS
                                true
                            }

                            PlayerUiState.CONTROLS -> {
                                if (episodes.isNotEmpty()) {
                                    uiState = PlayerUiState.EPISODE_INFO
                                }
                                true
                            }

                            PlayerUiState.EPISODE_INFO -> {
                                if (seasonEpisodes.isNotEmpty()) {
                                    uiState = PlayerUiState.EPISODE_NAVIGATION
                                }
                                true
                            }

                            PlayerUiState.EPISODE_NAVIGATION -> {
                                false
                            }
                        }
                    }

                    Key.DirectionUp -> {
                        when (uiState) {

                            PlayerUiState.FULLSCREEN -> {
                                false
                            }

                            PlayerUiState.CONTROLS -> {
                                uiState = PlayerUiState.FULLSCREEN
                                true
                            }

                            PlayerUiState.EPISODE_INFO -> {
                                uiState = PlayerUiState.FULLSCREEN
                                true
                            }

                            PlayerUiState.EPISODE_NAVIGATION -> {
                                uiState = PlayerUiState.EPISODE_INFO
                                true
                            }
                        }
                    }

                    Key.DirectionLeft -> {
                        when (uiState) {

                            PlayerUiState.FULLSCREEN -> {
                                uiState = PlayerUiState.CONTROLS
                                true
                            }

                            PlayerUiState.CONTROLS -> {
                                val target = (
                                        viewModel.exoPlayer.currentPosition -
                                                SEEK_STEP_MS
                                        ).coerceAtLeast(0L)

                                viewModel.exoPlayer.seekTo(target)
                                true
                            }

                            PlayerUiState.EPISODE_INFO,
                            PlayerUiState.EPISODE_NAVIGATION -> {
                                false
                            }
                        }
                    }

                    Key.DirectionRight -> {
                        when (uiState) {

                            PlayerUiState.FULLSCREEN -> {
                                uiState = PlayerUiState.CONTROLS
                                true
                            }

                            PlayerUiState.CONTROLS -> {
                                val currentPosition =
                                    viewModel.exoPlayer.currentPosition

                                val duration =
                                    viewModel.exoPlayer.duration

                                val target = currentPosition + SEEK_STEP_MS

                                viewModel.exoPlayer.seekTo(
                                    if (duration > 0L) {
                                        target.coerceAtMost(duration)
                                    } else {
                                        target
                                    }
                                )

                                true
                            }

                            PlayerUiState.EPISODE_INFO,
                            PlayerUiState.EPISODE_NAVIGATION -> {
                                false
                            }
                        }
                    }

                    else -> false
                }
            }
            .focusable()
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.exoPlayer

                    useController = false

                    setShowBuffering(
                        PlayerView.SHOW_BUFFERING_WHEN_PLAYING
                    )

                    keepScreenOn = true

                    isFocusable = false
                    isFocusableInTouchMode = false
                }
            },
            update = { playerView ->
                if (playerView.player !== viewModel.exoPlayer) {
                    playerView.player = viewModel.exoPlayer
                }
            }
        )

        if (uiState == PlayerUiState.CONTROLS) {
            PlaybackControlsOverlay(
                title = currentEpisodeTitle,
                position = playerPosition,
                duration = playerDuration,
                isPlaying = viewModel.exoPlayer.isPlaying,
                controlsFocusRequester = controlsFocusRequester,
                onPlayPause = {
                    if (viewModel.exoPlayer.isPlaying) {
                        viewModel.exoPlayer.pause()
                    } else {
                        viewModel.exoPlayer.play()
                    }

                    interactionCounter++
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (
            uiState == PlayerUiState.EPISODE_INFO ||
            uiState == PlayerUiState.EPISODE_NAVIGATION
        ) {
            EpisodeInfoOverlay(
                seriesTitle = title,
                episodeTitle = currentEpisodeTitle,
                description = currentEpisodeDescription,
                episodes = episodes,
                seasons = seasons,
                selectedSeason = selectedSeason,
                currentEpisodeFileName = currentEpisodeFileName,
                seasonFocusRequester = seasonFocusRequester,
                episodeFocusRequester = episodeFocusRequester,
                navigationEnabled = (
                        uiState == PlayerUiState.EPISODE_NAVIGATION
                        ),
                onSeasonSelected = { season ->
                    viewModel.selectSeason(season)
                    interactionCounter++
                },
                onEpisodeSelected = { episode ->

                    viewModel.playEpisode(
                        episode.fileName,
                        mediaItem.archiveIdentifier
                    )

                    uiState = PlayerUiState.FULLSCREEN
                },
                onInteraction = {
                    interactionCounter++
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PlaybackControlsOverlay(
    title: String,
    position: Long,
    duration: Long,
    isPlaying: Boolean,
    controlsFocusRequester: FocusRequester,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    var playButtonFocused by remember {
        mutableStateOf(false)
    }

    val progress = if (duration > 0L) {
        (position.toFloat() / duration.toFloat())
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OverlayBackground)
            .padding(
                horizontal = 42.dp,
                vertical = 28.dp
            )
    ) {

        Text(
            text = title,
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(PlayerRed)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = formatTime(position),
                color = Color.White,
                fontSize = 14.sp
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onPlayPause,
                modifier = Modifier
                    .focusRequester(controlsFocusRequester)
                    .onFocusChanged {
                        playButtonFocused = it.isFocused
                    }
                    .then(
                        if (playButtonFocused) {
                            Modifier.border(
                                2.dp,
                                Color.White,
                                RoundedCornerShape(8.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                Text(
                    text = if (isPlaying) "Pause" else "Play"
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = formatTime(duration),
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "← / →  10 seconds     ↓  Episodes",
            color = Color.LightGray,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun EpisodeInfoOverlay(
    seriesTitle: String,
    episodeTitle: String,
    description: String?,
    episodes: List<Episode>,
    seasons: List<Int>,
    selectedSeason: Int,
    currentEpisodeFileName: String,
    seasonFocusRequester: FocusRequester,
    episodeFocusRequester: FocusRequester,
    navigationEnabled: Boolean,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    onInteraction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val seasonEpisodes = remember(episodes, selectedSeason) {
        episodes
            .filter { it.seasonNumber == selectedSeason }
            .sortedBy { it.episodeNumber }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OverlayBackground)
            .padding(
                horizontal = 42.dp,
                vertical = 26.dp
            )
    ) {

        Text(
            text = seriesTitle,
            color = Color.LightGray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = episodeTitle,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        if (!description.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))

            Text(
                text = description,
                color = Color.LightGray,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                maxLines = 3
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = "Seasons",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            seasons.forEach { season ->

                SeasonChip(
                    season = season,
                    selected = season == selectedSeason,
                    focusRequester = if (season == selectedSeason) {
                        seasonFocusRequester
                    } else {
                        null
                    },
                    onClick = {
                        onSeasonSelected(season)
                    },
                    onFocused = onInteraction
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = if (navigationEnabled) {
                "Select episode"
            } else {
                "Press down to select episode"
            },
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            seasonEpisodes.forEachIndexed { index, episode ->

                EpisodeCard(
                    episode = episode,
                    selected = (
                            episode.fileName == currentEpisodeFileName
                            ),
                    focusRequester = if (index == 0) {
                        episodeFocusRequester
                    } else {
                        null
                    },
                    enabled = navigationEnabled,
                    onClick = {
                        onEpisodeSelected(episode)
                    },
                    onFocused = onInteraction
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = if (navigationEnabled) {
                "↑  Back to seasons"
            } else {
                "↓  Go to episodes"
            },
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SeasonChip(
    season: Int,
    selected: Boolean,
    focusRequester: FocusRequester?,
    onClick: () -> Unit,
    onFocused: () -> Unit
) {
    var focused by remember(season) {
        mutableStateOf(false)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .then(
                if (focusRequester != null) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                }
            )
            .onFocusChanged {
                if (it.isFocused && !focused) {
                    onFocused()
                }

                focused = it.isFocused
            },
        shape = RoundedCornerShape(24.dp),
        color = if (selected) {
            PlayerRed
        } else {
            Color(0xFF333333)
        },
        contentColor = Color.White,
        border = if (focused) {
            BorderStroke(2.dp, Color.White)
        } else {
            null
        }
    ) {
        Text(
            text = "Season $season",
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 11.dp
            ),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EpisodeCard(
    episode: Episode,
    selected: Boolean,
    focusRequester: FocusRequester?,
    enabled: Boolean,
    onClick: () -> Unit,
    onFocused: () -> Unit
) {
    var focused by remember(episode.fileName) {
        mutableStateOf(false)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(205.dp)
            .focusProperties {
                canFocus = enabled
            }
            .then(
                if (focusRequester != null) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                }
            )
            .onFocusChanged {
                if (it.isFocused && !focused) {
                    onFocused()
                }

                focused = it.isFocused
            },
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) {
            Color(0xFF4A171B)
        } else {
            Color(0xFF252525)
        },
        contentColor = Color.White,
        border = when {
            focused -> BorderStroke(3.dp, Color.White)
            selected -> BorderStroke(2.dp, PlayerRed)
            else -> null
        }
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {

            if (!episode.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = episode.thumbnailUrl,
                    contentDescription = episode.displayName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF333333)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF333333)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "S${episode.seasonNumber} · E${episode.episodeNumber}",
                color = PlayerRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = episode.displayName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2
            )

            if (!episode.description.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))

                Text(
                    text = episode.description,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000L)
        .coerceAtLeast(0L)

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(
            hours,
            minutes,
            seconds
        )
    } else {
        "%02d:%02d".format(
            minutes,
            seconds
        )
    }
}
