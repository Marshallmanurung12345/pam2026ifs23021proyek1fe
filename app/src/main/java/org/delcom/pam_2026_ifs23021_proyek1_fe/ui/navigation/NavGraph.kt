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
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem.LaundryServiceFormScreen
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ServiceCreate : Screen("services/create")
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

        // MainScreen = host 4 tab bottom nav
        composable(Screen.Home.route) {
            MainScreen(
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToServiceCreate = {
                    navController.navigate(Screen.ServiceCreate.route)
                },
                onNavigateToServiceEdit = { id ->
                    navController.navigate(Screen.ServiceEdit.createRoute(id))
                }
            )
        }

        // Form Tambah Layanan (Create)
        composable(Screen.ServiceCreate.route) {
            LaundryServiceFormScreen(
                token = token ?: "",
                serviceId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // Form Edit Layanan (Update)
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