package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryServiceFormScreen(
    token: String,
    serviceId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: LaundryServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = serviceId != null

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var estimatedDays by remember { mutableStateOf("1") }
    var isActive by remember { mutableStateOf(true) }
    var initialized by remember { mutableStateOf(false) }

    // Load data saat edit
    LaunchedEffect(serviceId) {
        if (serviceId != null) viewModel.getById(token, serviceId)
    }

    // Isi form saat data dimuat (hanya sekali)
    LaunchedEffect(uiState.selectedService) {
        val svc = uiState.selectedService
        if (svc != null && !initialized && isEdit) {
            name = svc.name
            description = svc.description
            price = svc.price.toInt().toString()
            unit = svc.unit
            estimatedDays = svc.estimatedDays.toString()
            isActive = svc.isActive
            initialized = true
        }
    }

    // Navigasi setelah berhasil
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            onSaved()
        }
    }

    val isFormValid = name.isNotBlank() &&
            (price.toDoubleOrNull() ?: 0.0) > 0 &&
            unit.isNotBlank() &&
            (estimatedDays.toIntOrNull() ?: 0) > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Layanan" else "Tambah Layanan") },
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
            // Info section
            if (!isEdit) {
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
                        Text("Isi form di bawah untuk menambahkan layanan laundry baru.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Nama Layanan
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Layanan *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.LocalLaundryService, null) },
                isError = name.isEmpty() && uiState.error != null
            )

            // Deskripsi
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2, maxLines = 4,
                leadingIcon = { Icon(Icons.Filled.Description, null) }
            )

            // Harga & Satuan dalam satu baris
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) price = it },
                    label = { Text("Harga *") },
                    modifier = Modifier.weight(2f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("Rp") },
                    isError = price.isNotEmpty() && (price.toDoubleOrNull() ?: 0.0) <= 0
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Satuan *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("kg") }
                )
            }

            // Estimasi hari
            OutlinedTextField(
                value = estimatedDays,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d+$"))) estimatedDays = it },
                label = { Text("Estimasi Hari *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("hari") },
                leadingIcon = { Icon(Icons.Filled.Schedule, null) }
            )

            // Toggle Status Aktif
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (isActive) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff,
                            null,
                            tint = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                        Column {
                            Text("Status Layanan",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium)
                            Text(
                                if (isActive) "Layanan aktif & dapat digunakan"
                                else "Layanan nonaktif",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }

            // Error message
            uiState.error?.let { err ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.ErrorOutline, null,
                            Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(err,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Tombol simpan
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: return@Button
                    val d = estimatedDays.toIntOrNull() ?: return@Button
                    if (isEdit && serviceId != null) {
                        viewModel.update(token, serviceId, name, description, p, unit, d) {}
                    } else {
                        viewModel.create(token, name, description, p, unit, d) {}
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.isLoading && isFormValid,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        if (isEdit) Icons.Filled.Save else Icons.Filled.Add,
                        null, Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isEdit) "Simpan Perubahan" else "Tambah Layanan",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}