package org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository.LaundryOrderRepository
import javax.inject.Inject

data class LaundryOrderUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val orders: List<LaundryOrder> = emptyList(),
    val selectedOrder: LaundryOrder? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

@HiltViewModel
class LaundryOrderViewModel @Inject constructor(
    private val repo: LaundryOrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaundryOrderUiState())
    val uiState: StateFlow<LaundryOrderUiState> = _uiState.asStateFlow()

    private var currentSearch: String? = null
    private var currentStatus: String? = null

    fun loadOrders(token: String, search: String? = null, status: String? = null, refresh: Boolean = false) {
        viewModelScope.launch {
            val page = if (refresh) { currentSearch = search; currentStatus = status; 1 }
            else _uiState.value.currentPage

            if (page == 1) _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            else _uiState.value = _uiState.value.copy(isLoadingMore = true)

            repo.getAll(token, currentSearch, currentStatus, page).fold(
                onSuccess = { resp ->
                    val newOrders = resp.data?.laundryOrders ?: emptyList()
                    val existing = if (page == 1) emptyList() else _uiState.value.orders
                    val totalPages = resp.data?.pagination?.totalPages ?: 1
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isLoadingMore = false,
                        orders = existing + newOrders,
                        currentPage = page + 1,
                        totalPages = totalPages
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoadingMore = false, error = e.message)
                }
            )
        }
    }

    fun loadMore(token: String) {
        val s = _uiState.value
        if (!s.isLoadingMore && s.currentPage <= s.totalPages) loadOrders(token)
    }

    fun getById(token: String, id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repo.getById(token, id).fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(isLoading = false, selectedOrder = resp.data?.laundryOrder)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
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
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun updateStatus(token: String, id: String, status: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.updateStatus(token, id, status).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false); onSuccess() },
                onFailure = { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
            )
        }
    }

    fun delete(token: String, id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.delete(token, id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false); onSuccess() },
                onFailure = { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
            )
        }
    }

    fun clearMessages() { _uiState.value = _uiState.value.copy(error = null, successMessage = null) }
    fun clearSelected() { _uiState.value = _uiState.value.copy(selectedOrder = null) }
}