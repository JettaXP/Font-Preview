package com.jettax.fonts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jettax.fonts.ui.screens.FontPreviewScreen
import com.jettax.fonts.ui.screens.InstallFontScreen
import com.jettax.fonts.ui.screens.LocalFontsScreen
import com.jettax.fonts.ui.screens.OnlineFontsScreen
import com.jettax.fonts.viewmodel.FontViewModel

data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

@Composable
fun FontApp(viewModel: FontViewModel = viewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val navItems = listOf(
        NavItem("Онлайн", Icons.Filled.Language, Icons.Outlined.Language, "online"),
        NavItem("Локальные", Icons.Filled.Folder, Icons.Outlined.Folder, "local"),
        NavItem("Установка", Icons.Filled.Download, Icons.Outlined.Download, "install"),
    )

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "online",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            composable("online") {
                OnlineFontsScreen(
                    viewModel = viewModel,
                    onFontClick = { font ->
                        viewModel.selectFont(font)
                        navController.navigate("preview/${font.family}")
                    }
                )
            }
            composable("local") {
                LocalFontsScreen(
                    viewModel = viewModel,
                    onFontClick = { font ->
                        viewModel.selectFont(font)
                        navController.navigate("preview/${font.family}")
                    }
                )
            }
            composable("install") {
                InstallFontScreen(viewModel = viewModel)
            }
            composable("preview/{fontFamily}") { backStackEntry ->
                val fontFamily = backStackEntry.arguments?.getString("fontFamily") ?: ""
                FontPreviewScreen(
                    fontFamily = fontFamily,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
