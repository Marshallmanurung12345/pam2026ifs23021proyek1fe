package org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.Order
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository.OrderRepository
import javax.inject.Inject

data class OrderUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val selectedOrder: Order? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val isLoadingMore: Boolean = false,
    val currentFilter: String? = null
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private var currentSearch: String? = null
    private var currentStatus: String? = null

    fun loadOrders(
        token: String,
        search: String? = null,
        status: String? = null,
        refresh: Boolean = false
    ) {
        viewModelScope.launch {
            val page = if (refresh) 1 else _uiState.value.currentPage
            if (page == 1) {
                currentSearch = search
                currentStatus = status
            }

            if (page == 1) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            }

            val result = repository.getOrders(token, page, 10, currentSearch, currentStatus)
            result.fold(
                onSuccess = { response ->
                    val newOrders = response.data?.orders ?: emptyList()
                    val existing = if (page == 1) emptyList() else _uiState.value.orders
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        orders = existing + newOrders,
                        currentPage = (response.data?.page ?: 1) + 1,
                        totalPages = response.data?.totalPages ?: 1,
                        currentFilter = status,
                        error = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun loadMore(token: String) {
        val state = _uiState.value
        if (!state.isLoadingMore && state.currentPage <= state.totalPages) {
            loadOrders(token)
        }
    }

    fun getOrder(token: String, id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getOrder(token, id)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedOrder = response.data
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun createOrder(
        token: String,
        laundryItemId: Int,
        customerName: String,
        weight: Double,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.createOrder(token, laundryItemId, customerName, weight, notes)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                    if (response.success) onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun updateStatus(token: String, id: Int, status: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.updateOrderStatus(token, id, status)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                    if (response.success) onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun deleteOrder(token: String, id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.deleteOrder(token, id)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                    if (response.success) onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun clearSelectedOrder() {
        _uiState.value = _uiState.value.copy(selectedOrder = null)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}