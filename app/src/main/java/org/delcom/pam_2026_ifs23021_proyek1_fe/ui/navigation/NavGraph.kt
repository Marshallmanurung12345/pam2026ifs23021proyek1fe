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
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order.OrderCreateScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object OrderCreate : Screen("order/create")
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

    // State untuk pre-selected service saat order dari Beranda
    var preselectedServiceId by remember { mutableStateOf<String?>(null) }
    var preselectedServiceName by remember { mutableStateOf<String?>(null) }
    var preselectedServicePrice by remember { mutableStateOf(0.0) }
    var preselectedServiceUnit by remember { mutableStateOf("") }
    var preselectedServiceDays by remember { mutableStateOf(1) }

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
                onNavigateToCreateOrder = { svc ->
                    // Simpan service yang dipilih lalu buka form
                    if (svc != null) {
                        preselectedServiceId = svc.id
                        preselectedServiceName = svc.name
                        preselectedServicePrice = svc.price
                        preselectedServiceUnit = svc.unit
                        preselectedServiceDays = svc.estimatedDays
                    } else {
                        preselectedServiceId = null
                    }
                    navController.navigate(Screen.OrderCreate.route)
                }
            )
        }

        composable(Screen.OrderCreate.route) {
            // Rekonstruksi LaundryService dari state jika ada
            val preselected = if (preselectedServiceId != null) {
                org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryService(
                    id = preselectedServiceId!!,
                    userId = "",
                    name = preselectedServiceName ?: "",
                    description = "",
                    price = preselectedServicePrice,
                    unit = preselectedServiceUnit,
                    estimatedDays = preselectedServiceDays,
                    isActive = true
                )
            } else null

            OrderCreateScreen(
                token = token ?: "",
                preselectedService = preselected,
                onBack = { navController.popBackStack() },
                onCreated = {
                    preselectedServiceId = null
                    navController.popBackStack()
                }
            )
        }
    }
}