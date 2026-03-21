package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.ConfirmDialog
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val userName by authViewModel.userName.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Keluar",
            message = "Apakah Anda yakin ingin keluar?",
            confirmLabel = "Keluar",
            onConfirm = { showLogoutDialog = false; authViewModel.logout(); onLogout() },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Avatar
        Box(
            Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                (userName?.firstOrNull()?.uppercaseChar() ?: 'U').toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(userName ?: "Pengguna", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        // Info Card
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Informasi Akun", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Person, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Nama", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(userName ?: "-", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.LocalLaundryService, null, Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Aplikasi", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("LaundryKu v1.0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Logout, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Keluar", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))
    }
}