package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryService
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryServiceViewModel

@OptIn(FlowPreview::class)
@Composable
fun LaundryServiceListScreen(
    token: String,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: LaundryServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val searchFlow = remember { MutableStateFlow("") }

    LaunchedEffect(searchFlow) {
        searchFlow.debounce(400).collectLatest { q ->
            if (token.isNotEmpty()) viewModel.loadServices(token, q)
        }
    }
    LaunchedEffect(token) { if (token.isNotEmpty()) viewModel.loadServices(token) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            SearchBar(query = searchQuery, onQueryChange = { searchQuery = it; searchFlow.value = it },
                placeholder = "Cari layanan...", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            when {
                uiState.isLoading && uiState.services.isEmpty() -> LoadingBox()
                uiState.error != null -> ErrorMessage(uiState.error!!) { viewModel.loadServices(token) }
                uiState.services.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState("Belum ada layanan")
                }
                else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.services, key = { it.id }) { svc ->
                        ServiceCard(svc) { onNavigateToDetail(svc.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: LaundryService, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocalLaundryService, null, Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(service.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (service.description.isNotEmpty())
                    Text(service.description, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("Rp ${"%,.0f".format(service.price)}/${service.unit}",
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("${service.estimatedDays} hari",
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    if (!service.isActive) {
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.errorContainer) {
                            Text("Nonaktif", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}