package org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.LaundryItem
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository.LaundryItemRepository
import javax.inject.Inject

data class LaundryItemUiState(
    val isLoading: Boolean = false,
    val items: List<LaundryItem> = emptyList(),
    val selectedItem: LaundryItem? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val isLoadingMore: Boolean = false
)

@HiltViewModel
class LaundryItemViewModel @Inject constructor(
    private val repository: LaundryItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaundryItemUiState())
    val uiState: StateFlow<LaundryItemUiState> = _uiState.asStateFlow()

    private var currentSearch: String? = null

    fun loadItems(token: String, search: String? = null, refresh: Boolean = false) {
        viewModelScope.launch {
            val page = if (refresh) 1 else _uiState.value.currentPage
            if (page == 1) currentSearch = search

            if (page == 1) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            }

            val result = repository.getLaundryItems(token, page, 10, currentSearch)
            result.fold(
                onSuccess = { response ->
                    val newItems = response.data?.items ?: emptyList()
                    val existing = if (page == 1) emptyList() else _uiState.value.items
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        items = existing + newItems,
                        currentPage = (response.data?.page ?: 1) + 1,
                        totalPages = response.data?.totalPages ?: 1,
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
            loadItems(token)
        }
    }

    fun getItem(token: String, id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getLaundryItem(token, id)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedItem = response.data
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun createItem(
        token: String,
        name: String,
        description: String,
        pricePerKg: Double,
        estimatedDays: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.createLaundryItem(token, name, description, pricePerKg, estimatedDays)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                    // success == null dianggap berhasil jika tidak ada error
                    if (response.success != false) onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun updateItem(
        token: String,
        id: Int,
        name: String,
        description: String,
        pricePerKg: Double,
        estimatedDays: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.updateLaundryItem(token, id, name, description, pricePerKg, estimatedDays)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                    if (response.success != false) onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun deleteItem(token: String, id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.deleteLaundryItem(token, id)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                    if (response.success != false) onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun clearSelectedItem() {
        _uiState.value = _uiState.value.copy(selectedItem = null)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}