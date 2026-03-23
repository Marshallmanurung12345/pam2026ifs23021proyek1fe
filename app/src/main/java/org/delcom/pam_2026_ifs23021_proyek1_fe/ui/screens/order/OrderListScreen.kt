package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryOrder
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryOrderViewModel

@OptIn(FlowPreview::class)
@Composable
fun OrderListScreen(
    token: String,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onSessionExpired: (() -> Unit)? = null,
    viewModel: LaundryOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    val searchFlow = remember { MutableStateFlow("") }

    // Auto-logout jika token expired
    LaunchedEffect(uiState.tokenExpired) {
        if (uiState.tokenExpired) {
            onSessionExpired?.invoke()
        }
    }

    LaunchedEffect(searchFlow) {
        searchFlow.debounce(400).collectLatest { q ->
            if (token.isNotEmpty()) viewModel.loadOrders(token, q, selectedFilter, refresh = true)
        }
    }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) viewModel.loadOrders(token, refresh = true)
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            layoutInfo.totalItemsCount > 0 && lastVisible >= layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && token.isNotEmpty()) viewModel.loadMore(token)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, "Buat Pesanan", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { q -> searchQuery = q; searchFlow.value = q },
                placeholder = "Cari nama pelanggan...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter chips scrollable horizontal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mapOf(
                    null to "Semua",
                    "pending" to "Menunggu",
                    "processing" to "Diproses",
                    "done" to "Selesai",
                    "delivered" to "Terkirim",
                    "cancelled" to "Batal"
                ).forEach { (status, label) ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = {
                            selectedFilter = status
                            viewModel.loadOrders(token, searchQuery, status, refresh = true)
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            when {
                uiState.isLoading && uiState.orders.isEmpty() -> LoadingBox()
                uiState.error != null && uiState.orders.isEmpty() -> ErrorMessage(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadOrders(token, refresh = true) }
                )
                uiState.orders.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState("Tidak ada pesanan ditemukan")
                }
                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        OrderCard(order = order) { onNavigateToDetail(order.id) }
                    }
                    if (uiState.isLoadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: LaundryOrder, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(order.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                StatusChip(order.status)
            }
            Text(order.serviceName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${order.quantity} item", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Rp ${"%,.0f".format(order.totalPrice)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            if (!order.notes.isNullOrEmpty()) {
                Text("📝 ${order.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}