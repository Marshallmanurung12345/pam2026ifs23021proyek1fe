package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.home.HomeScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem.LaundryServiceDetailScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem.LaundryServiceListScreen
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

    // ID layanan yang dipilih dari Daftar Data
    var selectedServiceId by remember { mutableStateOf<String?>(null) }

    val title = when (currentRoute) {
        "main_home" -> "Beranda"
        "main_services" -> "Daftar Data"
        "main_detail" -> "Detail Data"
        "main_profile" -> "Profil"
        else -> "LaundryKu"
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
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
                // Tab 1: Beranda
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, "Beranda") },
                    label = { Text("Beranda", style = MaterialTheme.typography.labelSmall) },
                    selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == "main_home" } == true,
                    onClick = { navigateTo("main_home") }
                )
                // Tab 2: Daftar Data
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, "Daftar Data") },
                    label = { Text("Daftar Data", style = MaterialTheme.typography.labelSmall) },
                    selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == "main_services" } == true,
                    onClick = { navigateTo("main_services") }
                )
                // Tab 3: Detail Data
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Info, "Detail Data") },
                    label = { Text("Detail Data", style = MaterialTheme.typography.labelSmall) },
                    selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == "main_detail" } == true,
                    onClick = { navigateTo("main_detail") }
                )
                // Tab 4: Profil
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, "Profil") },
                    label = { Text("Profil", style = MaterialTheme.typography.labelSmall) },
                    selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == "main_profile" } == true,
                    onClick = { navigateTo("main_profile") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController,
            startDestination = "main_home",
            modifier = Modifier.padding(padding)
        ) {

            // ─── Screen 1: Beranda ─────────────────────────────────────────
            composable("main_home") {
                HomeScreen(
                    token = token ?: "",
                    authViewModel = authViewModel,
                    onNavigateToServices = { navigateTo("main_services") }
                )
            }

            // ─── Screen 2: Daftar Data ─────────────────────────────────────
            composable("main_services") {
                LaundryServiceListScreen(
                    token = token ?: "",
                    onNavigateToDetail = { id ->
                        selectedServiceId = id
                        navigateTo("main_detail")
                    },
                    onNavigateToCreate = onNavigateToServiceCreate,
                    onSessionExpired = {
                        authViewModel.logout()
                        onLogout()
                    }
                )
            }

            // ─── Screen 3: Detail Data ─────────────────────────────────────
            composable("main_detail") {
                DetailDataScreen(
                    token = token ?: "",
                    selectedServiceId = selectedServiceId,
                    onGoToList = { navigateTo("main_services") },
                    onEdit = { id -> onNavigateToServiceEdit(id) },
                    onDeleted = {
                        selectedServiceId = null
                        navigateTo("main_services")
                    }
                )
            }

            // ─── Screen 4: Profil ──────────────────────────────────────────
            composable("main_profile") {
                ProfileScreen(authViewModel = authViewModel, onLogout = onLogout)
            }
        }
    }
}

// Composable terpisah untuk Screen 3 — menghindari lambda context error
@Composable
private fun DetailDataScreen(
    token: String,
    selectedServiceId: String?,
    onGoToList: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit
) {
    if (selectedServiceId == null) {
        // Placeholder saat belum ada layanan dipilih
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Filled.TouchApp, null,
                    Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Pilih layanan dari Daftar Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Tap salah satu layanan di tab Daftar Data untuk melihat detailnya di sini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onGoToList,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Buka Daftar Data")
                }
            }
        }
    } else {
        LaundryServiceDetailScreen(
            serviceId = selectedServiceId,
            token = token,
            onBack = onGoToList,
            onEdit = onEdit,
            onDeleted = onDeleted
        )
    }
}