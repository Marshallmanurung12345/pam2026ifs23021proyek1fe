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
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Int,
    token: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.selectedOrder

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("") }

    LaunchedEffect(orderId) {
        viewModel.getOrder(token, orderId)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) viewModel.clearMessages()
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus Pesanan",
            message = "Yakin ingin menghapus pesanan #$orderId?",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteOrder(token, orderId) { onDeleted() }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showStatusDialog) {
        val statuses = listOf("pending", "processing", "done", "delivered", "cancelled")
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Ubah Status") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    statuses.forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedStatus == s,
                                onClick = { selectedStatus = s }
                            )
                            Spacer(Modifier.width(8.dp))
                            StatusChip(s)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showStatusDialog = false
                    viewModel.updateStatus(token, orderId, selectedStatus) {
                        viewModel.getOrder(token, orderId)
                    }
                }) { Text("Simpan") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showStatusDialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pesanan #$orderId") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
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
            uiState.isLoading -> LoadingBox(Modifier.padding(padding))
            order == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Pesanan tidak ditemukan")
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Pesanan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        StatusChip(order.status)
                    }
                }

                // Detail card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Informasi Pesanan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        HorizontalDivider()
                        InfoRow("Nama Pelanggan", order.customerName)
                        InfoRow("Layanan", order.laundryItemName)
                        InfoRow("Berat", "${order.weight} kg")
                        InfoRow("Total Harga", "Rp ${"%,.0f".format(order.totalPrice)}")
                        InfoRow("Estimasi Selesai", order.estimatedDone.ifEmpty { "-" })
                        if (order.notes.isNotEmpty()) {
                            InfoRow("Catatan", order.notes)
                        }
                        InfoRow("Dibuat", order.createdAt.take(10))
                    }
                }

                // Change status button
                Button(
                    onClick = {
                        selectedStatus = order.status
                        showStatusDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ubah Status Pesanan")
                }

                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}