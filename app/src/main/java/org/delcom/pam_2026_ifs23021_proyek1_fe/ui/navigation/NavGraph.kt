package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.auth.LoginScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.auth.RegisterScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object OrderDetail : Screen("orders/{orderId}") {
        fun createRoute(id: Int) = "orders/$id"
    }
    object OrderCreate : Screen("orders/create")
    object LaundryItemDetail : Screen("laundry-items/{itemId}") {
        fun createRoute(id: Int) = "laundry-items/$id"
    }
    object LaundryItemCreate : Screen("laundry-items/create")
    object LaundryItemEdit : Screen("laundry-items/{itemId}/edit") {
        fun createRoute(id: Int) = "laundry-items/$id/edit"
    }
}

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()
    val token by authViewModel.authToken.collectAsState()

    // Tunggu token selesai di-load dari DataStore
    // token == null bisa berarti "belum dimuat" atau "memang tidak ada"
    // Kita pakai flag initialized
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(token) {
        // Setelah pertama kali token di-emit (baik null maupun ada isinya), set initialized
        initialized = true
    }

    if (!initialized) {
        // Tampilkan loading sambil tunggu DataStore
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (token != null) Screen.Home.route else Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
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

        composable(
            Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStack ->
            val orderId = backStack.arguments?.getInt("orderId") ?: return@composable
            OrderDetailScreen(
                orderId = orderId,
                token = token ?: "",
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(Screen.OrderCreate.route) {
            OrderCreateScreen(
                token = token ?: "",
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }

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

        composable(Screen.LaundryItemCreate.route) {
            LaundryItemFormScreen(
                token = token ?: "",
                itemId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

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