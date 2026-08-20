package com.ooredoost.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ooredoost.app.ui.MainViewModel
import com.ooredoost.app.ui.screens.HomeScreen
import com.ooredoost.app.ui.screens.SettingsScreen
import com.ooredoost.app.ui.screens.SetupScreen
import com.ooredoost.app.ui.screens.StatsScreen
import com.ooredoost.app.ui.theme.DarkBackground
import com.ooredoost.app.ui.theme.DarkSurface
import com.ooredoost.app.ui.theme.OoredooRed
import com.ooredoost.app.ui.theme.TextMuted

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "الرئيسية", Icons.Rounded.Home)
    data object Stats : Screen("stats", "الإحصائيات", Icons.Rounded.Analytics)
    data object Settings : Screen("settings", "الإعدادات", Icons.Rounded.Settings)
    data object Setup : Screen("setup", "", Icons.Rounded.Settings)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavScreens = listOf(Screen.Home, Screen.Stats, Screen.Settings)

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (currentRoute != Screen.Setup.route) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Box(
                                    modifier = if (selected) {
                                        Modifier
                                            .clip(CircleShape)
                                            .background(OoredooRed.copy(alpha = 0.12f))
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    } else {
                                        Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    }
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.label,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OoredooRed,
                                selectedTextColor = OoredooRed,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = {
                    fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToSetup = {
                            navController.navigate(Screen.Setup.route)
                        }
                    )
                }

                composable(Screen.Stats.route) {
                    StatsScreen(viewModel = viewModel)
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }

                composable(Screen.Setup.route) {
                    SetupScreen(
                        viewModel = viewModel,
                        onSetupComplete = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
