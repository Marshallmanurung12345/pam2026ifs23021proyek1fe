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
        fun createRoute(id: String) = "orders/$id"
    }
    object OrderCreate : Screen("orders/create")
    object ServiceDetail : Screen("services/{serviceId}") {
        fun createRoute(id: String) = "services/$id"
    }
    object ServiceCreate : Screen("services/create")
    object ServiceEdit : Screen("services/{serviceId}/edit") {
        fun createRoute(id: String) = "services/$id/edit"
    }
}

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val token by authViewModel.authToken.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()

    // Tunggu DataStore selesai load (null = belum load, "" atau value = sudah load)
    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Beri waktu sebentar untuk DataStore emit pertama kali
        kotlinx.coroutines.delay(300)
        initialized = true
    }

    if (!initialized) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDest = if (token != null && token!!.isNotEmpty()) Screen.Home.route else Screen.Login.route

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

        composable(Screen.OrderDetail.route) { back ->
            val orderId = back.arguments?.getString("orderId") ?: return@composable
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

        composable(Screen.ServiceDetail.route) { back ->
            val serviceId = back.arguments?.getString("serviceId") ?: return@composable
            LaundryServiceDetailScreen(
                serviceId = serviceId,
                token = token ?: "",
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.ServiceEdit.createRoute(id)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(Screen.ServiceCreate.route) {
            LaundryServiceFormScreen(
                token = token ?: "",
                serviceId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.ServiceEdit.route) { back ->
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