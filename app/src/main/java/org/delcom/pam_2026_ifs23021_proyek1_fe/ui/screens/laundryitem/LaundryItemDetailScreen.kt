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
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryItemDetailScreen(
    itemId: Int,
    token: String,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onDeleted: () -> Unit,
    viewModel: LaundryItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val item = uiState.selectedItem
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) { viewModel.getItem(token, itemId) }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) viewModel.clearMessages()
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus Layanan",
            message = "Yakin ingin menghapus layanan \"${item?.name}\"?",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteItem(token, itemId) { onDeleted() }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Layanan") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = {
                    if (item != null) {
                        IconButton(onClick = { onEdit(itemId) }) {
                            Icon(Icons.Filled.Edit, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
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
            uiState.isLoading -> LoadingBox(Modifier.padding(padding))
            item == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Layanan tidak ditemukan")
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
                // Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocalLaundryService,
                            null,
                            Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (item.description.isNotEmpty()) {
                            Text(
                                item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Price & Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Informasi Layanan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        HorizontalDivider()
                        InfoRow("Harga per kg", "Rp ${"%,.0f".format(item.pricePerKg)}")
                        InfoRow("Estimasi Selesai", "${item.estimatedDays} hari kerja")
                        InfoRow("Ditambahkan", item.createdAt.take(10).ifEmpty { "-" })
                        InfoRow("Diperbarui", item.updatedAt.take(10).ifEmpty { "-" })
                    }
                }

                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}