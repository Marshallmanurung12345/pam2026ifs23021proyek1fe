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
    private val repo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val authToken: StateFlow<String?> = repo.authToken
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userName: StateFlow<String?> = repo.userName
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val username: StateFlow<String?> = repo.username
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userId: StateFlow<String?> = repo.userId
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repo.login(username, password).fold(
                onSuccess = { response ->
                    if (response.data?.authToken != null) {
                        _uiState.value = AuthUiState(isLoggedIn = true)
                    } else {
                        _uiState.value = AuthUiState(error = response.message ?: "Login gagal")
                    }
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(error = e.message ?: "Login gagal")
                }
            )
        }
    }

    fun register(name: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repo.register(name, username, password).fold(
                onSuccess = { response ->
                    _uiState.value = AuthUiState(
                        successMessage = response.message ?: "Registrasi berhasil! Silakan login."
                    )
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(error = e.message ?: "Registrasi gagal")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null, successMessage = null, isLoggedIn = false
        )
    }
}