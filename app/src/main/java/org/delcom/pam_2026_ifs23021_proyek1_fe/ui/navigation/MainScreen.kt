package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.home.HomeScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem.LaundryServiceListScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.profile.ProfileScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigateToServiceDetail: (String) -> Unit,
    onNavigateToServiceCreate: () -> Unit,
    onNavigateToServiceEdit: (String) -> Unit
) {
    val token by authViewModel.authToken.collectAsState()
    val isDarkMode by authViewModel.isDarkMode.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val title = when (currentRoute) {
        "main_home" -> "Beranda"
        "main_services" -> "Daftar Layanan"
        "main_profile" -> "Profil"
        else -> "LaundryKu"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { authViewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle tema",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                listOf(
                    Triple("main_home", Icons.Filled.Home, "Beranda"),
                    Triple("main_services", Icons.Filled.LocalLaundryService, "Daftar Data"),
                    Triple("main_profile", Icons.Filled.Person, "Profil")
                ).forEach { (route, icon, label) ->
                    NavigationBarItem(
                        icon = { Icon(icon, label) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        selected = navBackStackEntry?.destination?.hierarchy
                            ?.any { it.route == route } == true,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController,
            startDestination = "main_home",
            modifier = Modifier.padding(padding)
        ) {
            composable("main_home") {
                HomeScreen(
                    token = token ?: "",
                    authViewModel = authViewModel,
                    onNavigateToServices = { navController.navigate("main_services") }
                )
            }
            composable("main_services") {
                // Screen 2: Daftar Data — List semua layanan
                LaundryServiceListScreen(
                    token = token ?: "",
                    onNavigateToDetail = onNavigateToServiceDetail,
                    onNavigateToCreate = onNavigateToServiceCreate
                )
            }
            composable("main_profile") {
                // Screen 4: Profil
                ProfileScreen(authViewModel = authViewModel, onLogout = onLogout)
            }
        }
    }
}