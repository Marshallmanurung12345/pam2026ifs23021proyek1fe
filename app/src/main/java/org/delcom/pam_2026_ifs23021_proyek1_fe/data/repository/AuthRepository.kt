package org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository

import org.delcom.pam_2026_ifs23021_proyek1_fe.data.local.UserPreferences
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api.AuthApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val userPreferences: UserPreferences
) {
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val token = body.getToken()
                    val user = body.getUser()
                    if (token != null) {
                        userPreferences.saveAuthToken(token)
                    }
                    if (user != null) {
                        userPreferences.saveUserInfo(
                            user.id,
                            user.name,
                            user.username,
                            user.role
                        )
                    }
                }
                Result.success(body ?: AuthResponse(false, "Empty response"))
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, username: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApiService.register(RegisterRequest(name, username, password))
            if (response.isSuccessful) {
                val body = response.body()
                Result.success(body ?: AuthResponse(false, "Empty response"))
            } else {
                Result.failure(Exception("Register failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        userPreferences.clearAll()
    }

    fun getAuthToken() = userPreferences.authToken
    fun getName() = userPreferences.name
    fun getUsername() = userPreferences.username
    fun getRole() = userPreferences.role
    fun getUserId() = userPreferences.userId
}