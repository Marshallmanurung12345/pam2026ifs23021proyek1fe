package org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository

import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api.LaundryServiceApiService
import javax.inject.Inject
import javax.inject.Singleton

class TokenExpiredException(
    message: String = "Sesi habis, silakan login ulang"
) : Exception(message)

@Singleton
class LaundryServiceRepository @Inject constructor(
    private val api: LaundryServiceApiService
) {
    suspend fun getAll(token: String, search: String? = null, isActive: Boolean? = null)
            : Result<LaundryServiceListResponse> = try {
        val r = api.getAll("Bearer $token", search?.ifBlank { null }, isActive)
        when {
            r.isSuccessful -> Result.success(r.body() ?: LaundryServiceListResponse())
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            else -> Result.failure(Exception("Error ${r.code()}"))
        }
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getById(token: String, id: String): Result<LaundryServiceResponse> = try {
        val r = api.getById("Bearer $token", id)
        when {
            r.isSuccessful -> Result.success(r.body() ?: LaundryServiceResponse())
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            else -> Result.failure(Exception("Error ${r.code()}"))
        }
    } catch (e: Exception) { Result.failure(e) }

    suspend fun create(token: String, req: CreateLaundryServiceRequest): Result<BaseResponse> = try {
        val r = api.create("Bearer $token", req)
        when {
            r.isSuccessful -> Result.success(r.body() ?: BaseResponse(message = "Berhasil"))
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            else -> Result.failure(Exception("Gagal (${r.code()})"))
        }
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(token: String, id: String, req: CreateLaundryServiceRequest): Result<BaseResponse> = try {
        val r = api.update("Bearer $token", id, req)
        when {
            r.isSuccessful -> Result.success(r.body() ?: BaseResponse(message = "Berhasil"))
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            else -> Result.failure(Exception("Gagal (${r.code()})"))
        }
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(token: String, id: String): Result<BaseResponse> = try {
        val r = api.delete("Bearer $token", id)
        when {
            r.isSuccessful -> Result.success(r.body() ?: BaseResponse(message = "Berhasil"))
            r.code() == 401 || r.code() == 403 -> Result.failure(TokenExpiredException())
            else -> Result.failure(Exception("Gagal (${r.code()})"))
        }
    } catch (e: Exception) { Result.failure(e) }
}