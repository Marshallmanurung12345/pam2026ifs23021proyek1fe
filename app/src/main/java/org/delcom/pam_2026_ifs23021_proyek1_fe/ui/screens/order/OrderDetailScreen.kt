package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.StatusChip
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    token: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: LaundryOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.selectedOrder
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) { viewModel.getById(token, orderId) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Batalkan Pesanan") },
            text = { Text("Yakin ingin membatalkan pesanan ini?") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        viewModel.updateStatus(token, orderId, "cancelled") {
                            viewModel.getById(token, orderId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Ya, Batalkan") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) { Text("Tidak") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && order == null -> {
                Box(Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            order == null -> {
                Box(Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.ErrorOutline, null,
                            Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                        Text("Pesanan tidak ditemukan")
                        Button(onClick = { viewModel.getById(token, orderId) }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
            else -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status banner
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Status Pesanan",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            StatusChip(order.status)
                            val statusDesc = when (order.status) {
                                "pending" -> "Pesanan kamu sedang menunggu konfirmasi dari laundry."
                                "processing" -> "Pakaianmu sedang dalam proses pencucian."
                                "done" -> "Pakaianmu sudah selesai diproses! Siap diambil."
                                "delivered" -> "Pesanan sudah diantar. Terima kasih! 🎉"
                                "cancelled" -> "Pesanan ini telah dibatalkan."
                                else -> ""
                            }
                            if (statusDesc.isNotEmpty()) {
                                Text(statusDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Detail info
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Rincian Pesanan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            HorizontalDivider()
                            DetailRow(Icons.Filled.LocalLaundryService, "Layanan", order.serviceName)
                            DetailRow(Icons.Filled.Scale, "Jumlah", "${order.quantity} item")
                            DetailRow(Icons.Filled.Payments, "Total Harga",
                                "Rp ${"%,.0f".format(order.totalPrice)}",
                                valueColor = MaterialTheme.colorScheme.primary)
                            if (!order.notes.isNullOrEmpty()) {
                                DetailRow(Icons.Filled.Notes, "Catatan", order.notes)
                            }
                            HorizontalDivider()
                            DetailRow(Icons.Filled.AccessTime, "Dibuat",
                                order.createdAt?.take(10) ?: "-")
                        }
                    }

                    // Info estimasi
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text(
                                "Hubungi laundry jika ada pertanyaan mengenai pesananmu.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    // Tombol batalkan (hanya jika masih pending)
                    if (order.status == "pending") {
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Filled.Cancel, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Batalkan Pesanan")
                        }
                    }

                    uiState.error?.let { err ->
                        Surface(color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            Text(err, Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium, color = valueColor)
        }
    }
}