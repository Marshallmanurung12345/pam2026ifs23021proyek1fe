package org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository.LaundryServiceRepository
import javax.inject.Inject

data class LaundryServiceUiState(
    val isLoading: Boolean = false,
    val services: List<LaundryService> = emptyList(),
    val selectedService: LaundryService? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class LaundryServiceViewModel @Inject constructor(
    private val repo: LaundryServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaundryServiceUiState())
    val uiState: StateFlow<LaundryServiceUiState> = _uiState.asStateFlow()

    fun loadServices(token: String, search: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.getAll(token, search).fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        services = resp.data?.laundryServices ?: emptyList()
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun getById(token: String, id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repo.getById(token, id).fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedService = resp.data?.laundryService
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun create(token: String, name: String, description: String, price: Double,
               unit: String, estimatedDays: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.create(token, CreateLaundryServiceRequest(name, description, price, unit, estimatedDays)).fold(
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

    fun update(token: String, id: String, name: String, description: String,
               price: Double, unit: String, estimatedDays: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.update(token, id, CreateLaundryServiceRequest(name, description, price, unit, estimatedDays)).fold(
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
    fun clearSelected() { _uiState.value = _uiState.value.copy(selectedService = null) }
}