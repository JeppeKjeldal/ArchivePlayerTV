package com.archiveplayer.tv.features.directadd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage

@Composable
fun DirectAddScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: DirectAddViewModel = viewModel()
) {
    var urlInput by remember { mutableStateOf("") }
    var categoryName by remember { mutableStateOf("") }

    val preview by viewModel.preview.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add via Direct URL",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Paste Archive.org URL or Identifier") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { viewModel.fetchMetadata(urlInput) }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.fetchMetadata(urlInput) },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Fetch Metadata")
                }
            }

            if (preview != null) {
                Spacer(modifier = Modifier.height(32.dp))

                AsyncImage(
                    model = preview!!.thumbnailUrl,
                    contentDescription = preview!!.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(150.dp)
                        .height(225.dp)
                        .background(Color.DarkGray)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = preview!!.title,
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category (leave blank for Uncategorized)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.saveToLibrary(categoryName)
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
                        viewModel.saveToLibrary(categoryName)
                        onSaved()
                    }) {
                        Text("Save to Library")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}