package org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository.LaundryOrderRepository
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository.TokenExpiredException
import javax.inject.Inject

data class LaundryOrderUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val orders: List<LaundryOrder> = emptyList(),
    val selectedOrder: LaundryOrder? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false,
    // flag khusus: token tidak valid → NavGraph akan auto-logout
    val tokenExpired: Boolean = false
)

@HiltViewModel
class LaundryOrderViewModel @Inject constructor(
    private val repo: LaundryOrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaundryOrderUiState())
    val uiState: StateFlow<LaundryOrderUiState> = _uiState.asStateFlow()

    private var currentSearch: String? = null
    private var currentStatus: String? = null

    fun loadOrders(
        token: String,
        search: String? = null,
        status: String? = null,
        refresh: Boolean = false
    ) {
        val page = if (refresh) {
            currentSearch = search?.trim()?.ifBlank { null }
            currentStatus = status?.trim()?.ifBlank { null }
            1
        } else {
            _uiState.value.currentPage
        }

        if (!refresh && (_uiState.value.isLoadingMore || !_uiState.value.hasMore)) return

        viewModelScope.launch {
            if (page == 1) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, orders = emptyList())
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            }

            repo.getAll(token, currentSearch, currentStatus, page, 10).fold(
                onSuccess = { resp ->
                    val newOrders = resp.data?.laundryOrders ?: emptyList()
                    val pagination = resp.data?.pagination
                    val hasMore = pagination?.hasNext ?: false
                    val existing = if (page == 1) emptyList() else _uiState.value.orders
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        orders = existing + newOrders,
                        currentPage = page + 1,
                        totalPages = pagination?.totalPages ?: 1,
                        hasMore = hasMore,
                        error = null
                    )
                },
                onFailure = { e ->
                    if (e is TokenExpiredException) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, isLoadingMore = false, tokenExpired = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, isLoadingMore = false,
                            error = e.message ?: "Gagal memuat pesanan"
                        )
                    }
                }
            )
        }
    }

    fun loadMore(token: String) {
        val s = _uiState.value
        if (s.isLoading || s.isLoadingMore || !s.hasMore) return
        loadOrders(token, currentSearch, currentStatus, refresh = false)
    }

    fun getById(token: String, id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.getById(token, id).fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(isLoading = false, selectedOrder = resp.data?.laundryOrder)
                },
                onFailure = { e ->
                    if (e is TokenExpiredException) {
                        _uiState.value = _uiState.value.copy(isLoading = false, tokenExpired = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
                }
            )
        }
    }

    fun create(token: String, req: CreateOrderRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.create(token, req).fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = resp.message)
                    onSuccess()
                },
                onFailure = { e ->
                    if (e is TokenExpiredException) {
                        _uiState.value = _uiState.value.copy(isLoading = false, tokenExpired = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
                }
            )
        }
    }

    fun update(token: String, id: String, req: UpdateOrderRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.update(token, id, req).fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = resp.message)
                    onSuccess()
                },
                onFailure = { e ->
                    if (e is TokenExpiredException) {
                        _uiState.value = _uiState.value.copy(isLoading = false, tokenExpired = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
                }
            )
        }
    }

    fun updateStatus(token: String, id: String, status: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.updateStatus(token, id, status).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Status diperbarui")
                    onSuccess()
                },
                onFailure = { e ->
                    if (e is TokenExpiredException) {
                        _uiState.value = _uiState.value.copy(isLoading = false, tokenExpired = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
                }
            )
        }
    }

    fun delete(token: String, id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.delete(token, id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false); onSuccess() },
                onFailure = { e ->
                    if (e is TokenExpiredException) {
                        _uiState.value = _uiState.value.copy(isLoading = false, tokenExpired = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
                }
            )
        }
    }

    fun clearMessages() { _uiState.value = _uiState.value.copy(error = null, successMessage = null) }
    fun clearSelected() { _uiState.value = _uiState.value.copy(selectedOrder = null) }
}