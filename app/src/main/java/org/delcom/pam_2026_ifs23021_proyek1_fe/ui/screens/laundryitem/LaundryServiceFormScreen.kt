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
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryServiceFormScreen(
    token: String, serviceId: String?,
    onBack: () -> Unit, onSaved: () -> Unit,
    viewModel: LaundryServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = serviceId != null
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var estimatedDays by remember { mutableStateOf("1") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(serviceId) { if (serviceId != null) viewModel.getById(token, serviceId) }
    LaunchedEffect(uiState.selectedService) {
        val svc = uiState.selectedService
        if (svc != null && !initialized && isEdit) {
            name = svc.name; description = svc.description
            price = svc.price.toInt().toString(); unit = svc.unit
            estimatedDays = svc.estimatedDays.toString(); initialized = true
        }
    }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) { viewModel.clearMessages(); onSaved() }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isEdit) "Edit Layanan" else "Tambah Layanan") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer))
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Nama Layanan") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(description, { description = it }, label = { Text("Deskripsi") },
                        modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(price, { price = it }, label = { Text("Harga") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), prefix = { Text("Rp ") })
                    OutlinedTextField(unit, { unit = it }, label = { Text("Satuan (kg, pcs, dll)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(estimatedDays, { estimatedDays = it }, label = { Text("Estimasi Hari") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), suffix = { Text(" hari") })

                    uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

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
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !uiState.isLoading && name.isNotBlank() &&
                                (price.toDoubleOrNull() ?: 0.0) > 0 && unit.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else Text(if (isEdit) "Simpan Perubahan" else "Tambah Layanan", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}