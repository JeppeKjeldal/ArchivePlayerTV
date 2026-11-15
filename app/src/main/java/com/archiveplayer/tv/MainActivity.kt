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
    // NavController styrer navigationen
    val navController = rememberNavController()

    // NavHost definerer alle skærme (routes)
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onButtonClick = { navController.navigate("detail") }
            )
        }

        composable("detail") {
            DetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun HomeScreen(onButtonClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Home Screen",
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onButtonClick) {
                Text("Go to Detail")
            }
        }
    }
}

@Composable
fun DetailScreen(onBackClick: () -> Unit) {
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
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBackClick) {
                Text("Go Back")
            }
        }
    }
}