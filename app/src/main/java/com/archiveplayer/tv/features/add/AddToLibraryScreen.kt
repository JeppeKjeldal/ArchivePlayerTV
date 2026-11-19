package com.archiveplayer.tv.features.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.archiveplayer.tv.data.archive.ArchiveDetails
import com.archiveplayer.tv.data.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun AddToLibraryScreen(
    identifier: String,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddToLibraryViewModel = viewModel()
) {
    var details by remember { mutableStateOf<ArchiveDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var categoryName by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(identifier) {
        coroutineScope.launch {
            try {
                val response = RetrofitClient.archiveApi.getMetadata(identifier)
                if (response.isSuccessful) {
                    val metadata = response.body() ?: emptyMap()
                    details = ArchiveDetails.fromMetadata(identifier, metadata)
                }
            } catch (e: Exception) {
                // Handle error silently or log
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                Text(text = "Loading metadata...", color = Color.White)
            }
            details != null -> {
                val item = details!!
                Column(
                    modifier = Modifier.widthIn(max = 600.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add to Library",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AsyncImage(
                        model = item.thumbnailUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(150.dp)
                            .height(225.dp)
                            .background(Color.DarkGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text("Category (leave blank for Uncategorized)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.saveToLibrary(item.identifier, item.title, item.description, item.thumbnailUrl, categoryName)
                            onSaved()
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(onClick = onCancel) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            viewModel.saveToLibrary(item.identifier, item.title, item.description, item.thumbnailUrl, categoryName)
                            onSaved()
                        }) {
                            Text("Save to Library")
                        }
                    }
                }
            }
        }
    }
}