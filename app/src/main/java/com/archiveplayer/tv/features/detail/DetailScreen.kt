package com.archiveplayer.tv.features.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.archiveplayer.tv.data.local.MediaItemEntity

@Composable
fun DetailScreen(
    mediaItem: MediaItemEntity,
    onBackClick: () -> Unit,
    onAddToLibrary: (String) -> Unit,
    onPlayClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { onBackClick() }
                .focusable()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Back",
                color = Color(0xFF9CA3AF),
                fontSize = 16.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            AsyncImage(
                model = mediaItem.thumbnailUrl,
                contentDescription = mediaItem.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(300.dp)
                    .height(450.dp)
                    .background(Color.DarkGray)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = mediaItem.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = mediaItem.categoryName,
                    fontSize = 16.sp,
                    color = Color(0xFF9CA3AF)
                )

                Text(
                    text = mediaItem.description ?: "No description available.",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { onPlayClick(mediaItem.archiveIdentifier) },
                        modifier = Modifier.width(160.dp)
                    ) {
                        Text("Play")
                    }

                    Button(
                        onClick = { onAddToLibrary(mediaItem.archiveIdentifier) },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text("Add to Library")
                    }
                }
            }
        }
    }
}