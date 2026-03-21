package org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository

import org.delcom.pam_2026_ifs23021_proyek1_fe.data.local.UserPreferences
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api.AuthApiService
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api.UserApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val userApiService: UserApiService,
    private val userPreferences: UserPreferences
) {
    val authToken = userPreferences.authToken
    val userName = userPreferences.userName
    val username = userPreferences.username
    val userId = userPreferences.userId

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                val authToken = body?.data?.authToken
                val refreshToken = body?.data?.refreshToken ?: ""
                if (authToken != null) {
                    userPreferences.saveTokens(authToken, refreshToken)
                    // Ambil data user setelah login
                    try {
                        val userResp = userApiService.getMe("Bearer $authToken")
                        if (userResp.isSuccessful) {
                            val user = userResp.body()?.data?.user
                            if (user != null) {
                                userPreferences.saveUserInfo(user.id, user.name, user.username)
                            }
                        }
                    } catch (_: Exception) {}
                }
                Result.success(body ?: AuthResponse(message = "Empty response"))
            } else {
                val msg = when (response.code()) {
                    404 -> "Username atau password salah"
                    else -> "Login gagal (${response.code()})"
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, username: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApiService.register(RegisterRequest(name, username, password))
            if (response.isSuccessful) {
                Result.success(response.body() ?: AuthResponse(message = "Registrasi berhasil"))
            } else {
                val msg = when (response.code()) {
                    409 -> "Username sudah digunakan"
                    else -> "Registrasi gagal (${response.code()})"
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() = userPreferences.clearAll()
}