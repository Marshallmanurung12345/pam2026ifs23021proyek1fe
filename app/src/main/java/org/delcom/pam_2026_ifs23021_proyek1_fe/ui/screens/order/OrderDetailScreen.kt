package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.UpdateOrderRequest
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryOrderViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    token: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: LaundryOrderViewModel = hiltViewModel(),
    serviceViewModel: LaundryServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val serviceState by serviceViewModel.uiState.collectAsState()
    val order = uiState.selectedOrder

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("") }

    // Edit form state
    var editCustomerName by remember { mutableStateOf("") }
    var editCustomerPhone by remember { mutableStateOf("") }
    var editQuantity by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }

    LaunchedEffect(orderId) {
        viewModel.getById(token, orderId)
        serviceViewModel.loadServices(token)
    }

    // Isi form edit saat order berhasil dimuat
    LaunchedEffect(order) {
        order?.let {
            editCustomerName = it.customerName
            editCustomerPhone = it.customerPhone
            editQuantity = it.quantity.toString()
            editNotes = it.notes ?: ""
        }
    }

    // Navigasi balik setelah berhasil update
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null && showEditDialog) {
            viewModel.clearMessages()
            showEditDialog = false
            viewModel.getById(token, orderId) // reload
        }
    }

    // ─── Delete Dialog ─────────────────────────────────────────────────────
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus Pesanan",
            message = "Yakin ingin menghapus pesanan milik \"${order?.customerName}\"?",
            confirmLabel = "Hapus",
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete(token, orderId) { onDeleted() }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // ─── Status Dialog ─────────────────────────────────────────────────────
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Ubah Status Pesanan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "pending" to "Menunggu",
                        "processing" to "Sedang Diproses",
                        "done" to "Selesai",
                        "delivered" to "Sudah Diantar",
                        "cancelled" to "Dibatalkan"
                    ).forEach { (status, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status }
                            )
                            Spacer(Modifier.width(8.dp))
                            StatusChip(status)
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStatusDialog = false
                        viewModel.updateStatus(token, orderId, selectedStatus) {
                            viewModel.getById(token, orderId)
                        }
                    },
                    enabled = selectedStatus.isNotEmpty()
                ) { Text("Simpan") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showStatusDialog = false }) { Text("Batal") }
            }
        )
    }

    // ─── Edit Dialog ────────────────────────────────────────────────────────
    if (showEditDialog && order != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Pesanan") },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editCustomerName,
                        onValueChange = { editCustomerName = it },
                        label = { Text("Nama Pelanggan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editCustomerPhone,
                        onValueChange = { editCustomerPhone = it },
                        label = { Text("No. Telepon") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = editQuantity,
                        onValueChange = { q ->
                            if (q.isEmpty() || q.matches(Regex("^\\d*\\.?\\d*$"))) editQuantity = q
                        },
                        label = { Text("Jumlah") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Catatan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = editQuantity.toDoubleOrNull() ?: return@Button
                        // Cari harga dari service berdasarkan serviceName
                        val svc = serviceState.services.find { it.id == order.serviceId }
                        val price = svc?.price ?: (order.totalPrice / order.quantity)
                        viewModel.update(
                            token, orderId,
                            UpdateOrderRequest(
                                serviceId = order.serviceId,
                                customerName = editCustomerName.trim(),
                                customerPhone = editCustomerPhone.trim(),
                                quantity = qty,
                                totalPrice = price * qty,
                                notes = editNotes.ifBlank { null }
                            )
                        ) {}
                    },
                    enabled = !uiState.isLoading &&
                            editCustomerName.isNotBlank() &&
                            editCustomerPhone.isNotBlank() &&
                            (editQuantity.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(
                        Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    else Text("Simpan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                },
                actions = {
                    if (order != null) {
                        IconButton(onClick = {
                            editCustomerName = order.customerName
                            editCustomerPhone = order.customerPhone
                            editQuantity = order.quantity.toString()
                            editNotes = order.notes ?: ""
                            showEditDialog = true
                        }) {
                            Icon(Icons.Filled.Edit, "Edit", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
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
            uiState.isLoading && order == null -> LoadingBox(Modifier.padding(padding))
            order == null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.ErrorOutline, null,
                        Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text("Pesanan tidak ditemukan")
                    OutlinedButton(onClick = { viewModel.getById(token, orderId) }) {
                        Text("Coba Lagi")
                    }
                }
            }
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status card
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Status Pesanan",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            StatusChip(order.status)
                        }
                        TextButton(onClick = { selectedStatus = order.status; showStatusDialog = true }) {
                            Icon(Icons.Filled.Edit, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Ubah")
                        }
                    }
                }

                // Informasi pesanan
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Informasi Pesanan",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        HorizontalDivider()
                        InfoRow("Pelanggan", order.customerName)
                        InfoRow("No. Telepon", order.customerPhone)
                        InfoRow("Layanan", order.serviceName)
                        InfoRow("Jumlah", "${order.quantity}")
                        InfoRow("Total", "Rp ${"%,.0f".format(order.totalPrice)}")
                        if (!order.notes.isNullOrEmpty()) {
                            InfoRow("Catatan", order.notes)
                        }
                        HorizontalDivider()
                        InfoRow("Dibuat", order.createdAt?.take(10) ?: "-")
                        if (!order.updatedAt.isNullOrEmpty()) {
                            InfoRow("Diperbarui", order.updatedAt.take(10))
                        }
                    }
                }

                // Error message
                uiState.error?.let { err ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            err,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}