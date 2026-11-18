package com.archiveplayer.tv.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.archiveplayer.tv.data.archive.ArchiveItem
import com.archiveplayer.tv.data.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    onItemSelected: (ArchiveItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ArchiveItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val searchFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    fun performSearch() {
        if (searchQuery.isBlank()) {
            errorMessage = "Please enter a search term"
            return
        }

        errorMessage = null
        isLoading = true
        searchResults = emptyList()

        coroutineScope.launch {
            try {
                val query = "mediatype:(movies OR tv OR animation) AND ($searchQuery)"
                val response = RetrofitClient.archiveApi.search(
                    query = query,
                    rows = 15
                )

                if (response.isSuccessful) {
                    val docs = response.body()?.response?.docs ?: emptyList()
                    searchResults = docs.map { ArchiveItem.fromDoc(it) }
                    if (searchResults.isEmpty()) {
                        errorMessage = "No results found"
                    }
                } else {
                    errorMessage = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Connection error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
    ) {
        Text(
            text = "Search Archive.org",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search title or keyword") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { performSearch() }
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(searchFocusRequester)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { performSearch() },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Searching..." else "Search")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (searchResults.isNotEmpty()) {
            Text(
                text = "Results (${searchResults.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = searchResults,
                    key = { it.identifier }
                ) { item ->
                    SearchResultItem(
                        item = item,
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        searchFocusRequester.requestFocus()
    }
}

@Composable
fun SearchResultItem(
    item: ArchiveItem,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() }
            .background(if (isFocused) Color.DarkGray else Color.Black)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = "Type: ${item.mediaType} | ID: ${item.identifier}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }
    }
}