package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.home.HomeScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order.OrderListScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem.LaundryItemListScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.profile.ProfileScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigateToOrderDetail: (Int) -> Unit,
    onNavigateToOrderCreate: () -> Unit,
    onNavigateToItemDetail: (Int) -> Unit,
    onNavigateToItemCreate: () -> Unit,
    onNavigateToItemEdit: (Int) -> Unit
) {
    val token by authViewModel.authToken.collectAsState()
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Beranda", Icons.Filled.Home, "main_home"),
        BottomNavItem("Pesanan", Icons.Filled.List, "main_orders"),
        BottomNavItem("Layanan", Icons.Filled.LocalLaundryService, "main_items"),
        BottomNavItem("Profil", Icons.Filled.Person, "main_profile")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentRoute = currentDestination?.route
    val currentTitle = when (currentRoute) {
        "main_home" -> "Beranda"
        "main_orders" -> "Daftar Pesanan"
        "main_items" -> "Layanan Laundry"
        "main_profile" -> "Profil Saya"
        else -> "Laundry App"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main_home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("main_home") {
                HomeScreen(
                    token = token ?: "",
                    authViewModel = authViewModel,
                    onNavigateToOrders = { navController.navigate("main_orders") },
                    onNavigateToItems = { navController.navigate("main_items") }
                )
            }
            composable("main_orders") {
                OrderListScreen(
                    token = token ?: "",
                    onNavigateToDetail = onNavigateToOrderDetail,
                    onNavigateToCreate = onNavigateToOrderCreate
                )
            }
            composable("main_items") {
                LaundryItemListScreen(
                    token = token ?: "",
                    onNavigateToDetail = onNavigateToItemDetail,
                    onNavigateToCreate = onNavigateToItemCreate
                )
            }
            composable("main_profile") {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}