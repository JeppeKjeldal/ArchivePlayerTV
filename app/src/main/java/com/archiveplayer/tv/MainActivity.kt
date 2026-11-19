package com.archiveplayer.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.MaterialTheme
import com.archiveplayer.tv.features.add.AddToLibraryScreen
import com.archiveplayer.tv.features.library.LibraryScreen
import com.archiveplayer.tv.features.library.LibraryViewModel
import com.archiveplayer.tv.features.login.LoginScreen
import com.archiveplayer.tv.features.login.LoginViewModel

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
    val loginViewModel: LoginViewModel = viewModel()
    val libraryViewModel: LibraryViewModel = viewModel()

    val profile by libraryViewModel.userProfile.collectAsState(initial = null)

    LaunchedEffect(profile) {
        if (profile != null) {
            navController.navigate("library") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                loginViewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("library") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("library") {
            LibraryScreen(
                libraryViewModel = libraryViewModel,
                onNavigateToAddSource = {
                    navController.navigate("search")
                },
                onItemClicked = { archiveIdentifier ->
                    navController.navigate("detail/$archiveIdentifier")
                }
            )
        }

        composable("search") {
            com.archiveplayer.tv.features.search.SearchScreen(
                onItemSelected = { item ->
                    navController.navigate("detail/${item.identifier}")
                }
            )
        }

        composable("detail/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: "Unknown"
            com.archiveplayer.tv.features.detail.DetailScreen(
                itemId = itemId,
                onBackClick = { navController.popBackStack() },
                onAddToLibrary = { identifier ->
                    navController.navigate("add_to_library/$identifier")
                }
            )
        }

        composable("add_to_library/{identifier}") { backStackEntry ->
            val identifier = backStackEntry.arguments?.getString("identifier") ?: ""
            AddToLibraryScreen(
                identifier = identifier,
                onSaved = {
                    navController.navigate("library") {
                        popUpTo("library") { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}