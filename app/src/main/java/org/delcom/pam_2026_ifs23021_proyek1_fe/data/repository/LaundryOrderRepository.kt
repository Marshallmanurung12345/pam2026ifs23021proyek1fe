package org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository

import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api.LaundryOrderApiService
import javax.inject.Inject
import javax.inject.Singleton

class TokenExpiredException(message: String = "Sesi habis, silakan login ulang") : Exception(message)

@Singleton
class LaundryOrderRepository @Inject constructor(
    private val api: LaundryOrderApiService
) {
    suspend fun getAll(
        token: String,
        search: String? = null,
        status: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): Result<LaundryOrderListResponse> = try {
        val cleanSearch = search?.trim()?.ifBlank { null }
        val cleanStatus = status?.trim()?.ifBlank { null }
        val r = api.getAll("Bearer $token", cleanSearch, cleanStatus, page, limit)
        when {
            r.isSuccessful -> Result.success(r.body() ?: LaundryOrderListResponse())
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            r.code() == 500 -> {
                val bodyMsg = try {
                    val body = r.errorBody()?.string() ?: ""
                    if (body.contains("Token", ignoreCase = true) || body.contains("valid", ignoreCase = true))
                        "Sesi habis, silakan login ulang"
                    else "Server error (500)"
                } catch (e: Exception) { "Server error (500)" }
                Result.failure(Exception(bodyMsg))
            }
            else -> Result.failure(Exception("Error ${r.code()}"))
        }
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getById(token: String, id: String): Result<LaundryOrderResponse> = try {
        val r = api.getById("Bearer $token", id)
        when {
            r.isSuccessful -> Result.success(r.body() ?: LaundryOrderResponse())
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            else -> Result.failure(Exception("Error ${r.code()}"))
        }
    } catch (e: Exception) { Result.failure(e) }

    suspend fun create(token: String, req: CreateOrderRequest): Result<BaseResponse> = try {
        val r = api.create("Bearer $token", req)
        when {
            r.isSuccessful -> Result.success(r.body() ?: BaseResponse(message = "Pesanan berhasil dibuat"))
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            r.code() == 400 -> Result.failure(Exception("Data pesanan tidak valid"))
            r.code() == 404 -> Result.failure(Exception("Layanan tidak ditemukan"))
            else -> Result.failure(Exception("Gagal membuat pesanan (${r.code()})"))
        }
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(token: String, id: String, req: UpdateOrderRequest): Result<BaseResponse> = try {
        val r = api.update("Bearer $token", id, req)
        when {
            r.isSuccessful -> Result.success(r.body() ?: BaseResponse(message = "Berhasil diperbarui"))
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            else -> Result.failure(Exception("Gagal memperbarui (${r.code()})"))
        }
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateStatus(token: String, id: String, status: String): Result<BaseResponse> = try {
        val r = api.updateStatus("Bearer $token", id, UpdateOrderStatusRequest(status))
        when {
            r.isSuccessful -> Result.success(r.body() ?: BaseResponse(message = "Status diperbarui"))
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            else -> Result.failure(Exception("Gagal ubah status (${r.code()})"))
        }
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(token: String, id: String): Result<BaseResponse> = try {
        val r = api.delete("Bearer $token", id)
        when {
            r.isSuccessful -> Result.success(r.body() ?: BaseResponse(message = "Berhasil dihapus"))
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            else -> Result.failure(Exception("Gagal menghapus (${r.code()})"))
        }
    } catch (e: Exception) { Result.failure(e) }
}