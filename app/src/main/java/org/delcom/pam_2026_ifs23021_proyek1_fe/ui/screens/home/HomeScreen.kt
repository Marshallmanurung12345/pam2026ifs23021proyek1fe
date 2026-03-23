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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryOrder
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.StatusChip
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.AuthViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryOrderViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryServiceViewModel

@Composable
fun HomeScreen(
    token: String,
    authViewModel: AuthViewModel,
    onNavigateToOrders: () -> Unit,
    onNavigateToServices: () -> Unit,
    onSessionExpired: (() -> Unit)? = null,
    orderViewModel: LaundryOrderViewModel = hiltViewModel(),
    serviceViewModel: LaundryServiceViewModel = hiltViewModel()
) {
    val name by authViewModel.userName.collectAsState()
    val orderState by orderViewModel.uiState.collectAsState()
    val serviceState by serviceViewModel.uiState.collectAsState()

    LaunchedEffect(orderState.tokenExpired) {
        if (orderState.tokenExpired) onSessionExpired?.invoke()
    }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            orderViewModel.loadOrders(token, refresh = true)
            serviceViewModel.loadServices(token)
        }
    }

    val orders = orderState.orders
    val totalOrders = orders.size
    val pendingCount = orders.count { it.status == "pending" }
    val processingCount = orders.count { it.status == "processing" }
    val doneCount = orders.count { it.status == "done" || it.status == "delivered" }
    val cancelledCount = orders.count { it.status == "cancelled" }
    val totalRevenue = orders
        .filter { it.status == "done" || it.status == "delivered" }
        .sumOf { it.totalPrice }
    val activeServices = serviceState.services.count { it.isActive }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (name?.firstOrNull()?.uppercaseChar() ?: 'U').toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineSmall,
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Revenue Card
        if (totalRevenue > 0) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Total Pendapatan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Text(
                            "Rp ${"%,.0f".format(totalRevenue)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            "dari $doneCount pesanan selesai",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Filled.TrendingUp, null,
                        Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Statistik
        Text("Ringkasan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = "Total Pesanan",
                    value = totalOrders.toString(),
                    icon = Icons.Filled.Receipt,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Menunggu",
                    value = pendingCount.toString(),
                    icon = Icons.Filled.HourglassTop,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = "Diproses",
                    value = processingCount.toString(),
                    icon = Icons.Filled.LocalLaundryService,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                StatCard(
                    label = "Selesai",
                    value = doneCount.toString(),
                    icon = Icons.Filled.CheckCircle,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = "Dibatalkan",
                    value = cancelledCount.toString(),
                    icon = Icons.Filled.Cancel,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatCard(
                    label = "Layanan Aktif",
                    value = activeServices.toString(),
                    icon = Icons.Filled.Storefront,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Akses Cepat
        Text("Akses Cepat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                onClick = onNavigateToOrders,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Receipt, null, Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Pesanan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (pendingCount > 0) {
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.error) {
                            Text(
                                "$pendingCount menunggu",
                                Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }
            Card(
                onClick = onNavigateToServices,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.LocalLaundryService, null, Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        "Layanan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "$activeServices aktif",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Pesanan Terbaru
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pesanan Terbaru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (orders.size > 5) {
                TextButton(onClick = onNavigateToOrders) {
                    Text("Lihat Semua")
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(16.dp))
                }
            }
        }

        if (orderState.isLoading) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (orders.isEmpty()) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Inbox, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("Belum ada pesanan", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            orders.take(5).forEach { order -> OrderSummaryCard(order) }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, null, Modifier.size(28.dp), tint = contentColor)
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(order: LaundryOrder) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    order.customerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    order.serviceName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Rp ${"%,.0f".format(order.totalPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            StatusChip(order.status)
        }
    }
}