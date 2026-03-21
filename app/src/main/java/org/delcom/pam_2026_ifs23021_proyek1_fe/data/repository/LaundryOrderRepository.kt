package org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository

import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api.LaundryOrderApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LaundryOrderRepository @Inject constructor(
    private val api: LaundryOrderApiService
) {
    suspend fun getAll(
        token: String, search: String? = null,
        status: String? = null, page: Int = 1, limit: Int = 10
    ): Result<LaundryOrderListResponse> = try {
        val r = api.getAll("Bearer $token", search?.ifBlank { null }, status?.ifBlank { null }, page, limit)
        if (r.isSuccessful) Result.success(r.body()!!)
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getById(token: String, id: String): Result<LaundryOrderResponse> = try {
        val r = api.getById("Bearer $token", id)
        if (r.isSuccessful) Result.success(r.body()!!)
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun create(token: String, req: CreateOrderRequest): Result<BaseResponse> = try {
        val r = api.create("Bearer $token", req)
        if (r.isSuccessful) Result.success(r.body()!!)
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(token: String, id: String, req: UpdateOrderRequest): Result<BaseResponse> = try {
        val r = api.update("Bearer $token", id, req)
        if (r.isSuccessful) Result.success(r.body()!!)
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateStatus(token: String, id: String, status: String): Result<BaseResponse> = try {
        val r = api.updateStatus("Bearer $token", id, UpdateOrderStatusRequest(status))
        if (r.isSuccessful) Result.success(r.body()!!)
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(token: String, id: String): Result<BaseResponse> = try {
        val r = api.delete("Bearer $token", id)
        if (r.isSuccessful) Result.success(r.body()!!)
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }
}