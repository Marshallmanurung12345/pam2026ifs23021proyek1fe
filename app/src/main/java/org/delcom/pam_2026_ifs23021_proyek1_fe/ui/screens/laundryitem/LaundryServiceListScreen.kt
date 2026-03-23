package org.delcom.pam_2026_ifs23021_proyek1_fe.ui.screens.laundryitem

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryService
import org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel.LaundryServiceViewModel

@Composable
fun LaundryServiceListScreen(
    token: String,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: LaundryServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // State lokal untuk search & filter
    var searchQuery by remember { mutableStateOf("") }
    var filterActive by remember { mutableStateOf<Boolean?>(null) } // null=semua, true=aktif, false=nonaktif

    // Load awal
    LaunchedEffect(token) {
        if (token.isNotEmpty()) viewModel.loadServices(token)
    }

    // Filter lokal dari semua data (search + filter)
    val filteredServices = remember(uiState.services, searchQuery, filterActive) {
        uiState.services.filter { svc ->
            val matchSearch = searchQuery.isBlank() ||
                    svc.name.contains(searchQuery, ignoreCase = true) ||
                    svc.description.contains(searchQuery, ignoreCase = true)
            val matchFilter = filterActive == null || svc.isActive == filterActive
            matchSearch && matchFilter
        }
    }

    // Infinite scroll — simulasi: tampilkan bertahap setiap 10 item
    var visibleCount by remember { mutableStateOf(10) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && visibleCount < filteredServices.size) {
            kotlinx.coroutines.delay(300) // efek loading
            visibleCount += 10
        }
    }
    // Reset visible count saat filter berubah
    LaunchedEffect(searchQuery, filterActive) { visibleCount = 10 }

    val displayedServices = filteredServices.take(visibleCount)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Tambah Layanan",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // ─── Search Bar ────────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Cari nama atau deskripsi layanan...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // ─── Filter Chips ──────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterActive == null,
                    onClick = { filterActive = null },
                    label = { Text("Semua", style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(50)
                )
                FilterChip(
                    selected = filterActive == true,
                    onClick = { filterActive = true },
                    label = { Text("Aktif", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        if (filterActive == true)
                            Icon(Icons.Filled.Check, null, Modifier.size(14.dp))
                    },
                    shape = RoundedCornerShape(50)
                )
                FilterChip(
                    selected = filterActive == false,
                    onClick = { filterActive = false },
                    label = { Text("Nonaktif", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        if (filterActive == false)
                            Icon(Icons.Filled.Check, null, Modifier.size(14.dp))
                    },
                    shape = RoundedCornerShape(50)
                )
            }

            // ─── Jumlah hasil ───────────────────────────────────────────────
            if (!uiState.isLoading) {
                Text(
                    "${filteredServices.size} layanan ditemukan" +
                            if (visibleCount < filteredServices.size) " · menampilkan $visibleCount" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            // ─── Content ────────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Memuat layanan...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                uiState.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.ErrorOutline, null,
                                Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                            Text(uiState.error!!, color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium)
                            Button(onClick = { viewModel.loadServices(token) }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                filteredServices.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.SearchOff, null,
                                Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                            Text(
                                if (searchQuery.isNotEmpty() || filterActive != null)
                                    "Tidak ada layanan yang cocok"
                                else "Belum ada layanan",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (searchQuery.isNotEmpty() || filterActive != null) {
                                TextButton(onClick = { searchQuery = ""; filterActive = null }) {
                                    Text("Reset Filter")
                                }
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 4.dp, bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayedServices, key = { it.id }) { svc ->
                            ServiceCard(svc) { onNavigateToDetail(svc.id) }
                        }
                        // Indikator loading more (infinite scroll)
                        if (visibleCount < filteredServices.size) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(Modifier.size(20.dp),
                                            strokeWidth = 2.dp)
                                        Text("Memuat lebih banyak...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: LaundryService, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Ikon layanan
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocalLaundryService, null,
                        Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Nama + badge status
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(service.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (service.isActive)
                            MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            if (service.isActive) "Aktif" else "Nonaktif",
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (service.isActive)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (service.description.isNotEmpty()) {
                    Text(service.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1)
                }

                // Harga & estimasi
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("Rp ${"%,.0f".format(service.price)}/${service.unit}",
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Surface(shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text("${service.estimatedDays} hari",
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            Icon(Icons.Filled.ChevronRight, null,
                tint = MaterialTheme.colorScheme.outline)
        }
    }
}