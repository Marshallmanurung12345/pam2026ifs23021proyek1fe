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
    val orders: List<LaundryOrder> = emptyList(),
    val selectedOrder: LaundryOrder? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val tokenExpired: Boolean = false
)

@HiltViewModel
class LaundryOrderViewModel @Inject constructor(
    private val repo: LaundryOrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaundryOrderUiState())
    val uiState: StateFlow<LaundryOrderUiState> = _uiState.asStateFlow()

    fun loadOrders(token: String, refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.getAll(token, search = null, status = null, page = 1, limit = 100).fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = resp.data?.laundryOrders ?: emptyList()
                    )
                },
                onFailure = { e ->
                    if (e is TokenExpiredException) {
                        _uiState.value = _uiState.value.copy(isLoading = false, tokenExpired = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Gagal memuat pesanan"
                        )
                    }
                }
            )
        }
    }

    fun getById(token: String, id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.getById(token, id).fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedOrder = resp.data?.laundryOrder
                    )
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = resp.message ?: "Pesanan berhasil dikirim!"
                    )
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
            repo.updateStatus(token, id, status).fold(
                onSuccess = { onSuccess() },
                onFailure = { e ->
                    if (e is TokenExpiredException) {
                        _uiState.value = _uiState.value.copy(tokenExpired = true)
                    } else {
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    fun clearSelected() {
        _uiState.value = _uiState.value.copy(selectedOrder = null)
    }
}