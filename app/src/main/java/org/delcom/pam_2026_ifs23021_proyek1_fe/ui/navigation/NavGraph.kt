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

// ─── Route Definitions ─────────────────────────────────────────────────────
// PENTING: "create" harus didaftarkan SEBELUM "{id}" agar tidak konflik!
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")

    // Order routes
    object OrderCreate : Screen("orders/create")
    object OrderDetail : Screen("orders/{orderId}") {
        fun createRoute(id: String) = "orders/$id"
    }

    // Service routes
    object ServiceCreate : Screen("services/create")
    object ServiceDetail : Screen("services/{serviceId}") {
        fun createRoute(id: String) = "services/$id"
    }
    object ServiceEdit : Screen("services/{serviceId}/edit") {
        fun createRoute(id: String) = "services/$id/edit"
    }
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val token by authViewModel.authToken.collectAsState()

    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        initialized = true
    }

    if (!initialized) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDest = if (!token.isNullOrEmpty()) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDest, modifier = modifier) {

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
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
                onNavigateToOrderDetail = { id -> navController.navigate(Screen.OrderDetail.createRoute(id)) },
                onNavigateToOrderCreate = { navController.navigate(Screen.OrderCreate.route) },
                onNavigateToServiceDetail = { id -> navController.navigate(Screen.ServiceDetail.createRoute(id)) },
                onNavigateToServiceCreate = { navController.navigate(Screen.ServiceCreate.route) },
                onNavigateToServiceEdit = { id -> navController.navigate(Screen.ServiceEdit.createRoute(id)) }
            )
        }

        // ─── Order: create HARUS sebelum {orderId} ───────────────────────────
        composable(Screen.OrderCreate.route) {
            OrderCreateScreen(
                token = token ?: "",
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { back ->
            val orderId = back.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(
                orderId = orderId,
                token = token ?: "",
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }

        // ─── Service: create HARUS sebelum {serviceId} ───────────────────────
        composable(Screen.ServiceCreate.route) {
            LaundryServiceFormScreen(
                token = token ?: "",
                serviceId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ServiceDetail.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { back ->
            val serviceId = back.arguments?.getString("serviceId") ?: return@composable
            LaundryServiceDetailScreen(
                serviceId = serviceId,
                token = token ?: "",
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.ServiceEdit.createRoute(id)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ServiceEdit.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { back ->
            val serviceId = back.arguments?.getString("serviceId") ?: return@composable
            LaundryServiceFormScreen(
                token = token ?: "",
                serviceId = serviceId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}