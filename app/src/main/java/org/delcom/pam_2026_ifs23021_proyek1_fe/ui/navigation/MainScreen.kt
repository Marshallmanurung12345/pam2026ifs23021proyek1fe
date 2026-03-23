package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.home.HomeScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem.LaundryServiceDetailScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem.LaundryServiceListScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem.LaundryServiceFormScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.profile.ProfileScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigateToServiceCreate: () -> Unit,
    onNavigateToServiceEdit: (String) -> Unit
) {
    val token by authViewModel.authToken.collectAsState()
    val isDarkMode by authViewModel.isDarkMode.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // State: ID layanan yang dipilih dari Daftar Data → ditampilkan di Detail Data
    var selectedServiceId by remember { mutableStateOf<String?>(null) }

    val title = when {
        currentRoute == "main_home" -> "Beranda"
        currentRoute == "main_services" -> "Daftar Data"
        currentRoute == "main_detail" -> "Detail Data"
        currentRoute == "main_profile" -> "Profil"
        else -> "LaundryKu"
    }

    // Definisi 4 tab Bottom Navigation
    data class NavTab(
        val route: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val label: String
    )

    val tabs = listOf(
        NavTab("main_home", Icons.Filled.Home, "Beranda"),
        NavTab("main_services", Icons.Filled.FormatListBulleted, "Daftar Data"),
        NavTab("main_detail", Icons.Filled.Info, "Detail Data"),
        NavTab("main_profile", Icons.Filled.Person, "Profil")
    )

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
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        selected = navBackStackEntry?.destination?.hierarchy
                            ?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
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

            // ─── Screen 1: Beranda ─────────────────────────────────────────────
            composable("main_home") {
                HomeScreen(
                    token = token ?: "",
                    authViewModel = authViewModel,
                    onNavigateToServices = {
                        navController.navigate("main_services") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ─── Screen 2: Daftar Data ─────────────────────────────────────────
            composable("main_services") {
                LaundryServiceListScreen(
                    token = token ?: "",
                    onNavigateToDetail = { id ->
                        // Simpan ID yang dipilih, lalu navigasi ke tab Detail Data
                        selectedServiceId = id
                        navController.navigate("main_detail") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToCreate = onNavigateToServiceCreate
                )
            }

            // ─── Screen 3: Detail Data ─────────────────────────────────────────
            composable("main_detail") {
                if (selectedServiceId == null) {
                    // Belum ada layanan dipilih — tampilkan panduan
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Filled.TouchApp, null,
                                Modifier.padding(0.dp).let { it },
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Pilih layanan dari",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = {
                                navController.navigate("main_services") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                }
                            }) {
                                Icon(Icons.Filled.FormatListBulleted, null)
                                Spacer(Modifier.padding(4.dp))
                                Text("Buka Daftar Data")
                            }
                        }
                    }
                } else {
                    LaundryServiceDetailScreen(
                        serviceId = selectedServiceId!!,
                        token = token ?: "",
                        onBack = {
                            selectedServiceId = null
                            navController.navigate("main_services") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onEdit = { id -> onNavigateToServiceEdit(id) },
                        onDeleted = {
                            selectedServiceId = null
                            navController.navigate("main_services") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // ─── Screen 4: Profil ──────────────────────────────────────────────
            composable("main_profile") {
                ProfileScreen(authViewModel = authViewModel, onLogout = onLogout)
            }
        }
    }
}