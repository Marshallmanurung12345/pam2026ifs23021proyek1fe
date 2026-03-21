package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.ui.components.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.OrderViewModel

@OptIn(FlowPreview::class)
@Composable
fun OrderListScreen(
    token: String,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    val searchFlow = remember { MutableStateFlow("") }

    LaunchedEffect(searchFlow) {
        searchFlow.debounce(400).collectLatest { q ->
            if (token.isNotEmpty()) {
                viewModel.loadOrders(token, search = q, status = selectedFilter, refresh = true)
            }
        }
    }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) viewModel.loadOrders(token, refresh = true)
    }

    // Infinite scroll trigger
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 3 && total > 0
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isLoadingMore) {
            viewModel.loadMore(token)
        }
    }

    val filterOptions = listOf(null, "pending", "processing", "done", "delivered", "cancelled")
    val filterLabels = mapOf(
        null to "Semua",
        "pending" to "Menunggu",
        "processing" to "Diproses",
        "done" to "Selesai",
        "delivered" to "Terkirim",
        "cancelled" to "Batal"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        searchFlow.value = it
                    },
                    placeholder = "Cari nama pelanggan..."
                )

                // Filter chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    filterOptions.forEach { status ->
                        FilterChip(
                            selected = selectedFilter == status,
                            onClick = {
                                selectedFilter = status
                                viewModel.loadOrders(token, search = searchQuery, status = status, refresh = true)
                            },
                            label = { Text(filterLabels[status] ?: "Semua", style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            when {
                uiState.isLoading && uiState.orders.isEmpty() -> LoadingBox()
                uiState.error != null && uiState.orders.isEmpty() -> ErrorMessage(
                    uiState.error!!,
                    onRetry = { viewModel.loadOrders(token, refresh = true) }
                )
                uiState.orders.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState("Tidak ada pesanan ditemukan")
                }
                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        OrderCard(order = order, onClick = { onNavigateToDetail(order.id) })
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