package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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

    var selectedServiceId by remember { mutableStateOf<String?>(null) }

    val title = when (currentRoute) {
        "main_home" -> "Beranda"
        "main_list" -> "Daftar Data"
        "main_detail" -> "Detail Data"
        "main_profile" -> "Profil"
        else -> "LaundryKu"
    }

    fun goTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val onSessionExpired: () -> Unit = {
        authViewModel.logout()
        onLogout()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { authViewModel.toggleDarkMode() }) {
                        Icon(
                            if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            "Toggle tema",
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
                    Triple("main_list", Icons.Filled.List, "Daftar Data"),
                    Triple("main_detail", Icons.Filled.Info, "Detail Data"),
                    Triple("main_profile", Icons.Filled.Person, "Profil")
                ).forEach { (route, icon, label) ->
                    NavigationBarItem(
                        icon = { Icon(icon, label) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        selected = navBackStackEntry?.destination?.hierarchy
                            ?.any { it.route == route } == true,
                        onClick = { goTo(route) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "main_home",
            modifier = Modifier.padding(padding)) {

            // Screen 1: Beranda
            composable("main_home") {
                HomeScreen(
                    token = token ?: "",
                    authViewModel = authViewModel,
                    onNavigateToServices = { goTo("main_list") },
                    onSessionExpired = onSessionExpired
                )
            }

            // Screen 2: Daftar Data
            composable("main_list") {
                LaundryServiceListScreen(
                    token = token ?: "",
                    onNavigateToDetail = { id ->
                        selectedServiceId = id
                        goTo("main_detail")
                    },
                    onNavigateToCreate = onNavigateToServiceCreate,
                    onSessionExpired = onSessionExpired
                )
            }

            // Screen 3: Detail Data
            composable("main_detail") {
                DetailDataScreen(
                    token = token ?: "",
                    selectedServiceId = selectedServiceId,
                    onGoToList = { goTo("main_list") },
                    onEdit = { id -> onNavigateToServiceEdit(id) },
                    onDeleted = { selectedServiceId = null; goTo("main_list") },
                    onSessionExpired = onSessionExpired
                )
            }

            // Screen 4: Profil
            composable("main_profile") {
                ProfileScreen(authViewModel = authViewModel, onLogout = onLogout)
            }
        }
    }
}

@Composable
private fun DetailDataScreen(
    token: String,
    selectedServiceId: String?,
    onGoToList: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    onSessionExpired: () -> Unit
) {
    if (selectedServiceId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(Icons.Filled.TouchApp, null,
                    Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Text("Pilih layanan dari Daftar Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tap salah satu layanan untuk melihat detailnya di sini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGoToList, shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Filled.List, null, Modifier.size(18.dp))
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