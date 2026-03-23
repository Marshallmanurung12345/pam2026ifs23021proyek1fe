package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.CreateOrderRequest
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryService
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryOrderViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCreateScreen(
    token: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    orderViewModel: LaundryOrderViewModel = hiltViewModel(),
    serviceViewModel: LaundryServiceViewModel = hiltViewModel()
) {
    val orderState by orderViewModel.uiState.collectAsState()
    val serviceState by serviceViewModel.uiState.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf<LaundryService?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Load services saat screen dibuka
    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            serviceViewModel.loadServices(token)
        }
    }

    // Navigasi setelah berhasil membuat pesanan
    LaunchedEffect(orderState.successMessage) {
        if (orderState.successMessage != null) {
            orderViewModel.clearMessages()
            onCreated()
        }
    }

    val totalPrice = (selectedService?.price ?: 0.0) * (quantity.toDoubleOrNull() ?: 0.0)
    val isFormValid = customerName.isNotBlank() &&
            customerPhone.isNotBlank() &&
            selectedService != null &&
            (quantity.toDoubleOrNull() ?: 0.0) > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Data Pelanggan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Nama Pelanggan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = customerName.isEmpty() && orderState.error != null
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("No. Telepon") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = customerPhone.isEmpty() && orderState.error != null
                    )
                }
            }

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Detail Pesanan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // ─── Dropdown Fix: gunakan ExposedDropdownMenuBox dengan benar ───
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedService?.let {
                                "${it.name} — Rp ${"%.0f".format(it.price)}/${it.unit}"
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Layanan") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            // menuAnchor() tanpa parameter deprecated, gunakan versi baru
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            isError = selectedService == null && orderState.error != null
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            if (serviceState.isLoading) {
                                DropdownMenuItem(
                                    text = { Text("Memuat layanan...") },
                                    onClick = {},
                                    enabled = false
                                )
                            } else if (serviceState.services.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada layanan tersedia") },
                                    onClick = {},
                                    enabled = false
                                )
                            } else {
                                serviceState.services
                                    .filter { it.isActive }
                                    .forEach { svc ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(svc.name, fontWeight = FontWeight.Medium)
                                                    Text(
                                                        "Rp ${"%,.0f".format(svc.price)}/${svc.unit} · ${svc.estimatedDays} hari",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedService = svc
                                                dropdownExpanded = false
                                                // Reset quantity saat ganti layanan
                                                quantity = ""
                                            }
                                        )
                                    }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { q ->
                            // Hanya terima angka dan satu titik
                            if (q.isEmpty() || q.matches(Regex("^\\d*\\.?\\d*$"))) {
                                quantity = q
                            }
                        },
                        label = { Text("Jumlah") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = { Text(selectedService?.unit ?: "") },
                        enabled = selectedService != null,
                        isError = quantity.isNotEmpty() && (quantity.toDoubleOrNull() ?: 0.0) <= 0
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    // Total price preview
                    if (totalPrice > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Harga satuan:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Rp ${"%,.0f".format(selectedService?.price ?: 0.0)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                )
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Total:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Rp ${"%,.0f".format(totalPrice)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Error message
                    orderState.error?.let { err ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                err,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val svc = selectedService ?: return@Button
                            val qty = quantity.toDoubleOrNull() ?: return@Button
                            orderViewModel.create(
                                token,
                                CreateOrderRequest(
                                    serviceId = svc.id,
                                    customerName = customerName.trim(),
                                    customerPhone = customerPhone.trim(),
                                    quantity = qty,
                                    totalPrice = svc.price * qty,
                                    notes = notes.ifBlank { null }
                                )
                            ) {}
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !orderState.isLoading && isFormValid,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (orderState.isLoading) {
                            CircularProgressIndicator(
                                Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Buat Pesanan", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}