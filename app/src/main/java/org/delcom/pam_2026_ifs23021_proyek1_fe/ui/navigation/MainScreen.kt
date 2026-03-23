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
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryService
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.home.HomeScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order.OrderDetailScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order.OrderListScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.profile.ProfileScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigateToCreateOrder: (LaundryService?) -> Unit
) {
    val token by authViewModel.authToken.collectAsState()
    val isDarkMode by authViewModel.isDarkMode.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // State untuk detail pesanan yang dipilih
    var selectedOrderId by remember { mutableStateOf<String?>(null) }

    val title = when (currentRoute) {
        "main_home" -> "Beranda"
        "main_orders" -> "Pesanan Saya"
        "main_detail" -> "Detail Pesanan"
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

    val handleSessionExpired: () -> Unit = {
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
                // Tab 1: Beranda
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, "Beranda") },
                    label = { Text("Beranda", style = MaterialTheme.typography.labelSmall) },
                    selected = navBackStackEntry?.destination?.hierarchy
                        ?.any { it.route == "main_home" } == true,
                    onClick = { navigateTo("main_home") }
                )
                // Tab 2: Daftar Data (Pesanan)
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Receipt, "Pesanan") },
                    label = { Text("Daftar Data", style = MaterialTheme.typography.labelSmall) },
                    selected = navBackStackEntry?.destination?.hierarchy
                        ?.any { it.route == "main_orders" } == true,
                    onClick = { navigateTo("main_orders") }
                )
                // Tab 3: Detail Data
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Info, "Detail") },
                    label = { Text("Detail Data", style = MaterialTheme.typography.labelSmall) },
                    selected = navBackStackEntry?.destination?.hierarchy
                        ?.any { it.route == "main_detail" } == true,
                    onClick = { navigateTo("main_detail") }
                )
                // Tab 4: Profil
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, "Profil") },
                    label = { Text("Profil", style = MaterialTheme.typography.labelSmall) },
                    selected = navBackStackEntry?.destination?.hierarchy
                        ?.any { it.route == "main_profile" } == true,
                    onClick = { navigateTo("main_profile") }
                )
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "main_home",
            modifier = Modifier.padding(padding)) {

            // Screen 1: Beranda — lihat layanan, tombol Pesan langsung
            composable("main_home") {
                HomeScreen(
                    token = token ?: "",
                    authViewModel = authViewModel,
                    onNavigateToOrders = { navigateTo("main_orders") },
                    onBuatPesanan = { svc -> onNavigateToCreateOrder(svc) }
                )
            }

            // Screen 2: Daftar Data — semua pesanan pelanggan
            composable("main_orders") {
                OrderListScreen(
                    token = token ?: "",
                    onNavigateToDetail = { id ->
                        selectedOrderId = id
                        navigateTo("main_detail")
                    },
                    onNavigateToCreate = { onNavigateToCreateOrder(null) },
                    onSessionExpired = handleSessionExpired
                )
            }

            // Screen 3: Detail Data — detail pesanan yang dipilih
            composable("main_detail") {
                DetailDataScreen(
                    token = token ?: "",
                    selectedOrderId = selectedOrderId,
                    onGoToList = { navigateTo("main_orders") },
                    onDeleted = {
                        selectedOrderId = null
                        navigateTo("main_orders")
                    }
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
    selectedOrderId: String?,
    onGoToList: () -> Unit,
    onDeleted: () -> Unit
) {
    if (selectedOrderId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(Icons.Filled.TouchApp, null,
                    Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Text("Pilih pesanan dari Daftar Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tap salah satu pesanan untuk melihat detailnya di sini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGoToList, shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Filled.Receipt, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Buka Daftar Data")
                }
            }
        }
    } else {
        OrderDetailScreen(
            orderId = selectedOrderId,
            token = token,
            onBack = onGoToList,
            onDeleted = onDeleted
        )
    }
}