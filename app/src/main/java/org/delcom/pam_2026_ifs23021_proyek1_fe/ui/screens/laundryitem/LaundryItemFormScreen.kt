package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.LoadingBox
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryItemFormScreen(
    token: String,
    itemId: Int?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: LaundryItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = itemId != null

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pricePerKg by remember { mutableStateOf("") }
    var estimatedDays by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    // Load existing data if editing
    LaunchedEffect(itemId) {
        if (itemId != null) viewModel.getItem(token, itemId)
    }

    // Pre-fill form when data loaded
    LaunchedEffect(uiState.selectedItem) {
        val item = uiState.selectedItem
        if (item != null && !initialized && isEdit) {
            name = item.name
            description = item.description
            pricePerKg = item.pricePerKg.toInt().toString()
            estimatedDays = item.estimatedDays.toString()
            initialized = true
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Layanan" else "Tambah Layanan") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading && isEdit && !initialized) {
            LoadingBox(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            if (isEdit) "Edit Data Layanan" else "Data Layanan Baru",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama Layanan") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Contoh: Cuci Reguler") }
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Deskripsi") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            placeholder = { Text("Deskripsi layanan...") }
                        )

                        OutlinedTextField(
                            value = pricePerKg,
                            onValueChange = { pricePerKg = it },
                            label = { Text("Harga per kg (Rp)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            prefix = { Text("Rp ") }
                        )

                        OutlinedTextField(
                            value = estimatedDays,
                            onValueChange = { estimatedDays = it },
                            label = { Text("Estimasi Hari Selesai") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            suffix = { Text(" hari") }
                        )

                        uiState.error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        val isValid = name.isNotBlank() &&
                                (pricePerKg.toDoubleOrNull() ?: 0.0) > 0 &&
                                (estimatedDays.toIntOrNull() ?: 0) > 0

                        Button(
                            onClick = {
                                val price = pricePerKg.toDoubleOrNull() ?: return@Button
                                val days = estimatedDays.toIntOrNull() ?: return@Button
                                if (isEdit && itemId != null) {
                                    viewModel.updateItem(token, itemId, name, description, price, days) {}
                                } else {
                                    viewModel.createItem(token, name, description, price, days) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !uiState.isLoading && isValid,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    if (isEdit) "Simpan Perubahan" else "Tambah Layanan",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}