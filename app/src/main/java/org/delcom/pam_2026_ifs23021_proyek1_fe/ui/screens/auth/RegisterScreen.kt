package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.PasswordField
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMismatch by remember { mutableStateOf(false) }

    // Navigasi ke login setelah register berhasil
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            onRegisterSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Akun") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) { Icon(Icons.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()).padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Buat Akun Baru", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Isi data di bawah untuk mendaftar", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(8.dp))

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Nama Lengkap") },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(username, { username = it }, label = { Text("Username") },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    PasswordField(password, {
                        password = it
                        passwordMismatch = confirmPassword.isNotEmpty() && it != confirmPassword
                    })
                    PasswordField(confirmPassword, {
                        confirmPassword = it
                        passwordMismatch = it != password
                    }, label = "Konfirmasi Password")

                    if (passwordMismatch) {
                        Text("Password tidak cocok", color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            if (password != confirmPassword) { passwordMismatch = true; return@Button }
                            viewModel.register(name, username, password)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !uiState.isLoading && name.isNotBlank() && username.isNotBlank()
                                && password.isNotBlank() && !passwordMismatch,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else Text("Daftar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sudah punya akun?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onNavigateToLogin) { Text("Masuk", fontWeight = FontWeight.Bold) }
            }
        }
    }
}