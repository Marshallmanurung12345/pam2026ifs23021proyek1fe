package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import kotlinx.coroutines.flow.collectLatest
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.auth.LoginScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.auth.RegisterScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.home.HomeScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.profile.ProfileScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object OrderList : Screen("orders")
    object OrderDetail : Screen("orders/{orderId}") {
        fun createRoute(id: Int) = "orders/$id"
    }
    object OrderCreate : Screen("orders/create")
    object LaundryItemList : Screen("laundry-items")
    object LaundryItemDetail : Screen("laundry-items/{itemId}") {
        fun createRoute(id: Int) = "laundry-items/$id"
    }
    object LaundryItemCreate : Screen("laundry-items/create")
    object LaundryItemEdit : Screen("laundry-items/{itemId}/edit") {
        fun createRoute(id: Int) = "laundry-items/$id/edit"
    }
    object Profile : Screen("profile")
}

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val token by authViewModel.authToken.collectAsState()

    val startDestination = if (token != null) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Main with Bottom Nav
        composable(Screen.Home.route) {
            MainScreen(
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToOrderDetail = { id ->
                    navController.navigate(Screen.OrderDetail.createRoute(id))
                },
                onNavigateToOrderCreate = {
                    navController.navigate(Screen.OrderCreate.route)
                },
                onNavigateToItemDetail = { id ->
                    navController.navigate(Screen.LaundryItemDetail.createRoute(id))
                },
                onNavigateToItemCreate = {
                    navController.navigate(Screen.LaundryItemCreate.route)
                },
                onNavigateToItemEdit = { id ->
                    navController.navigate(Screen.LaundryItemEdit.createRoute(id))
                }
            )
        }

        // Order Detail
        composable(
            Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStack ->
            val orderId = backStack.arguments?.getInt("orderId") ?: return@composable
            OrderDetailScreen(
                orderId = orderId,
                token = token ?: "",
                onBack = { navController.popBackStack() },
                onDeleted = {
                    navController.popBackStack()
                }
            )
        }

        // Order Create
        composable(Screen.OrderCreate.route) {
            OrderCreateScreen(
                token = token ?: "",
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }

        // Laundry Item Detail
        composable(
            Screen.LaundryItemDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStack ->
            val itemId = backStack.arguments?.getInt("itemId") ?: return@composable
            LaundryItemDetailScreen(
                itemId = itemId,
                token = token ?: "",
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.LaundryItemEdit.createRoute(id)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        // Laundry Item Create
        composable(Screen.LaundryItemCreate.route) {
            LaundryItemFormScreen(
                token = token ?: "",
                itemId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // Laundry Item Edit
        composable(
            Screen.LaundryItemEdit.route,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStack ->
            val itemId = backStack.arguments?.getInt("itemId") ?: return@composable
            LaundryItemFormScreen(
                token = token ?: "",
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}