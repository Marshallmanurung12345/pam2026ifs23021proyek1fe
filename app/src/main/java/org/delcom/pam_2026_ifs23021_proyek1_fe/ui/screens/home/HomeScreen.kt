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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryOrder
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.StatusChip
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryOrderViewModel

@Composable
fun HomeScreen(
    token: String,
    authViewModel: AuthViewModel,
    onNavigateToOrders: () -> Unit,
    onNavigateToServices: () -> Unit,
    orderViewModel: LaundryOrderViewModel = hiltViewModel()
) {
    val name by authViewModel.userName.collectAsState()
    val orderState by orderViewModel.uiState.collectAsState()

    LaunchedEffect(token) {
        if (token.isNotEmpty()) orderViewModel.loadOrders(token, refresh = true)
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center) {
                    Text((name?.firstOrNull()?.uppercaseChar() ?: 'U').toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Selamat Datang,", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(name ?: "Pengguna", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Stats
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val total = orderState.orders.size
            val pending = orderState.orders.count { it.status == "pending" }
            val done = orderState.orders.count { it.status == "done" || it.status == "delivered" }
            StatCard("Total", total.toString(), Icons.Filled.List, Modifier.weight(1f))
            StatCard("Proses", pending.toString(), Icons.Filled.HourglassTop, Modifier.weight(1f))
            StatCard("Selesai", done.toString(), Icons.Filled.CheckCircle, Modifier.weight(1f))
        }

        // Quick Access
        Text("Akses Cepat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(onClick = onNavigateToOrders, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Receipt, null, Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.secondary)
                    Text("Pesanan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            Card(onClick = onNavigateToServices, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.LocalLaundryService, null, Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.tertiary)
                    Text("Layanan", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Recent Orders
        Text("Pesanan Terbaru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (orderState.isLoading) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (orderState.orders.isEmpty()) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Inbox, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("Belum ada pesanan", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            orderState.orders.take(5).forEach { order ->
                OrderSummaryCard(order)
            }
            if (orderState.orders.size > 5) {
                TextButton(onClick = onNavigateToOrders, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Lihat Semua Pesanan")
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OrderSummaryCard(order: LaundryOrder) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(order.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(order.serviceName, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Rp ${"%,.0f".format(order.totalPrice)}", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            StatusChip(order.status)
        }
    }
}