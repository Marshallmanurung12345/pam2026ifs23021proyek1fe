package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem

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
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryServiceDetailScreen(
    serviceId: String, token: String,
    onBack: () -> Unit, onEdit: (String) -> Unit, onDeleted: () -> Unit,
    viewModel: LaundryServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val service = uiState.selectedService
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(serviceId) { viewModel.getById(token, serviceId) }

    if (showDeleteDialog) {
        ConfirmDialog("Hapus Layanan", "Yakin ingin menghapus layanan \"${service?.name}\"?",
            onConfirm = { showDeleteDialog = false; viewModel.delete(token, serviceId) { onDeleted() } },
            onDismiss = { showDeleteDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Detail Layanan") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = {
                    if (service != null) {
                        IconButton(onClick = { onEdit(serviceId) }) {
                            Icon(Icons.Filled.Edit, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer))
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingBox(Modifier.padding(padding))
            service == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Layanan tidak ditemukan")
            }
            else -> Column(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                    .padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.LocalLaundryService, null, Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Text(service.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (service.description.isNotEmpty())
                            Text(service.description, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Detail Layanan", fontWeight = FontWeight.Bold)
                        HorizontalDivider()
                        InfoRow("Harga", "Rp ${"%,.0f".format(service.price)} / ${service.unit}")
                        InfoRow("Estimasi", "${service.estimatedDays} hari kerja")
                        InfoRow("Status", if (service.isActive) "Aktif" else "Nonaktif")
                    }
                }
            }
        }
    }
}