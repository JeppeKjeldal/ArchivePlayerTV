package com.archiveplayer.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.archiveplayer.tv.data.archive.ArchiveItem
import com.archiveplayer.tv.features.login.LoginScreen
import com.archiveplayer.tv.features.search.SearchScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ArchivePlayerApp()
            }
        }
    }
}

@Composable
fun ArchivePlayerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { profileName ->
                    navController.navigate("search")
                }
            )
        }

        composable("search") {
            SearchScreen(
                onItemSelected = { item ->
                    // For nu navigerer vi til detail med item.identifier
                    // Senere kan vi sende hele item-objektet
                    navController.navigate("detail/${item.identifier}")
                }
            )
        }

        composable("detail/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: "Unknown"
            DetailScreen(
                itemId = itemId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun DetailScreen(
    itemId: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Detail Screen",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Item ID: $itemId",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onBackClick) {
                Text("Go Back")
            }
        }
    }
}