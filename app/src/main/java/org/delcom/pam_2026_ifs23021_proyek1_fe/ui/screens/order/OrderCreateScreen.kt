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
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryItem
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.LoadingBox
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryItemViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCreateScreen(
    token: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    orderViewModel: OrderViewModel = hiltViewModel(),
    itemViewModel: LaundryItemViewModel = hiltViewModel()
) {
    val orderState by orderViewModel.uiState.collectAsState()
    val itemState by itemViewModel.uiState.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<LaundryItem?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) itemViewModel.loadItems(token, refresh = true)
    }

    LaunchedEffect(orderState.successMessage) {
        if (orderState.successMessage != null) {
            orderViewModel.clearMessages()
            onCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Pesanan Baru") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Detail Pesanan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Nama Pelanggan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Laundry Item dropdown
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedItem?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Layanan") },
                            trailingIcon = {
                                Icon(Icons.Filled.ArrowDropDown, null)
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            if (itemState.isLoading) {
                                DropdownMenuItem(text = { Text("Memuat...") }, onClick = {})
                            } else {
                                itemState.items.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(item.name, fontWeight = FontWeight.Medium)
                                                Text(
                                                    "Rp ${"%,.0f".format(item.pricePerKg)}/kg · ${item.estimatedDays} hari",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedItem = item
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Berat (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = { Text("kg") }
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    // Price preview
                    selectedItem?.let { item ->
                        val w = weight.toDoubleOrNull() ?: 0.0
                        val total = item.pricePerKg * w
                        if (w > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Estimasi Total:", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "Rp ${"%,.0f".format(total)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    orderState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            val w = weight.toDoubleOrNull() ?: 0.0
                            val item = selectedItem ?: return@Button
                            orderViewModel.createOrder(
                                token, item.id, customerName, w, notes
                            ) {}
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !orderState.isLoading && customerName.isNotBlank()
                                && selectedItem != null && (weight.toDoubleOrNull() ?: 0.0) > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (orderState.isLoading) {
                            CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Buat Pesanan", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}