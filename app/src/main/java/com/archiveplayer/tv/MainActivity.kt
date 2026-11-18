package com.archiveplayer.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.MaterialTheme
import com.archiveplayer.tv.features.detail.DetailScreen
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