package org.delcom.pam_2026_ifs23021_proyek1_fe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository.AuthRepository
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val authToken: StateFlow<String?> = authRepository.getAuthToken()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userName: StateFlow<String?> = authRepository.getName()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userRole: StateFlow<String?> = authRepository.getRole()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(username, password)
            result.fold(
                onSuccess = { response ->
                    val token = response.resolveToken()
                    if (token != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            error = response.message ?: "Login gagal, periksa username/password"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        error = e.message ?: "Login gagal"
                    )
                }
            )
        }
    }

    fun register(name: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.register(name, username, password)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message ?: "Registrasi berhasil",
                        error = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Registrasi gagal"
                    )
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}