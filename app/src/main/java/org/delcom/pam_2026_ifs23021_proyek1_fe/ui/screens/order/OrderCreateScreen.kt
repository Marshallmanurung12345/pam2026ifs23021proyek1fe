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
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.CreateOrderRequest
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryService
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryOrderViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCreateScreen(
    token: String,
    preselectedService: LaundryService? = null,
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
    var selectedService by remember { mutableStateOf(preselectedService) }
    var serviceExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) serviceViewModel.loadServices(token)
    }

    LaunchedEffect(orderState.successMessage) {
        if (orderState.successMessage != null) {
            orderViewModel.clearMessages()
            onCreated()
        }
    }

    val totalPrice = (selectedService?.price ?: 0.0) * (quantity.toDoubleOrNull() ?: 0.0)

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Data Diri",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Nama Lengkap *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Person, null) }
            )

            OutlinedTextField(
                value = customerPhone,
                onValueChange = { customerPhone = it },
                label = { Text("No. WhatsApp / Telepon *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Filled.Phone, null) }
            )

            HorizontalDivider()
            Text("Pilih Layanan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            // Dropdown layanan
            ExposedDropdownMenuBox(
                expanded = serviceExpanded,
                onExpandedChange = { serviceExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedService?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Layanan *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(serviceExpanded) },
                    modifier = Modifier.fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    leadingIcon = { Icon(Icons.Filled.LocalLaundryService, null) }
                )
                ExposedDropdownMenu(serviceExpanded, { serviceExpanded = false }) {
                    serviceState.services.filter { it.isActive }.forEach { svc ->
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
                                serviceExpanded = false
                                quantity = ""
                            }
                        )
                    }
                }
            }

            // Tampilkan detail layanan terpilih
            selectedService?.let { svc ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Info, null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Column {
                            Text(svc.name,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium)
                            Text("Rp ${"%,.0f".format(svc.price)}/${svc.unit} · estimasi ${svc.estimatedDays} hari",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) quantity = it },
                label = { Text("Jumlah *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text(selectedService?.unit ?: "") },
                enabled = selectedService != null,
                leadingIcon = { Icon(Icons.Filled.Scale, null) }
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan (opsional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 4,
                placeholder = { Text("Contoh: ada noda membandel, pakai pewangi extra") }
            )

            // Preview total
            if (totalPrice > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total yang harus dibayar",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Rp ${"%,.0f".format(totalPrice)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Icon(Icons.Filled.Payments, null,
                            Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    }
                }
            }

            orderState.error?.let { err ->
                Surface(color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.ErrorOutline, null, Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(err, color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    val svc = selectedService ?: return@Button
                    val qty = quantity.toDoubleOrNull() ?: return@Button
                    orderViewModel.create(token, CreateOrderRequest(
                        serviceId = svc.id,
                        customerName = customerName.trim(),
                        customerPhone = customerPhone.trim(),
                        quantity = qty,
                        totalPrice = svc.price * qty,
                        notes = notes.ifBlank { null }
                    )) {}
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !orderState.isLoading &&
                        customerName.isNotBlank() &&
                        customerPhone.isNotBlank() &&
                        selectedService != null &&
                        (quantity.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (orderState.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Send, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Kirim Pesanan", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}