package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.home

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.OrderCard
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.OrderViewModel

@Composable
fun HomeScreen(
    token: String,
    authViewModel: AuthViewModel,
    onNavigateToOrders: () -> Unit,
    onNavigateToItems: () -> Unit,
    orderViewModel: OrderViewModel = hiltViewModel()
) {
    val name by authViewModel.userName.collectAsState()
    val role by authViewModel.userRole.collectAsState()
    val orderUiState by orderViewModel.uiState.collectAsState()

    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            orderViewModel.loadOrders(token, refresh = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (name?.firstOrNull()?.uppercaseChar() ?: "U").toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        "Selamat Datang,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        name ?: "Pengguna",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            (role ?: "customer").uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val total = orderUiState.orders.size
            val pending = orderUiState.orders.count { it.status == "pending" }
            val done = orderUiState.orders.count { it.status == "done" || it.status == "delivered" }

            StatCard("Total", total.toString(), Icons.Filled.List, Modifier.weight(1f))
            StatCard("Proses", pending.toString(), Icons.Filled.HourglassTop, Modifier.weight(1f))
            StatCard("Selesai", done.toString(), Icons.Filled.CheckCircle, Modifier.weight(1f))
        }

        // Quick Access
        Text("Akses Cepat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAccessCard(
                label = "Pesanan",
                icon = Icons.Filled.Receipt,
                onClick = onNavigateToOrders,
                modifier = Modifier.weight(1f)
            )
            QuickAccessCard(
                label = "Layanan",
                icon = Icons.Filled.LocalLaundryService,
                onClick = onNavigateToItems,
                modifier = Modifier.weight(1f)
            )
        }

        // Recent Orders
        Text("Pesanan Terbaru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        if (orderUiState.isLoading) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (orderUiState.orders.isEmpty()) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(
                    Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.InboxOutlined, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("Belum ada pesanan", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            orderUiState.orders.take(5).forEach { order ->
                OrderCard(order = order, onClick = {})
            }
            if (orderUiState.orders.size > 5) {
                TextButton(
                    onClick = onNavigateToOrders,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Lihat Semua Pesanan")
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickAccessCard(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.secondary)
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}