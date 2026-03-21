package org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository

import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api.OrderApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderApiService: OrderApiService
) {
    suspend fun getOrders(
        token: String,
        page: Int = 1,
        perPage: Int = 10,
        search: String? = null,
        status: String? = null
    ): Result<OrderListResponse> {
        return try {
            val response = orderApiService.getOrders(
                "Bearer $token", page, perPage,
                search?.ifBlank { null },
                status?.ifBlank { null }
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: OrderListResponse(false, "Empty"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrder(token: String, id: Int): Result<OrderResponse> {
        return try {
            val response = orderApiService.getOrder("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(response.body() ?: OrderResponse(false, "Empty"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrder(
        token: String,
        laundryItemId: Int,
        customerName: String,
        weight: Double,
        notes: String
    ): Result<OrderResponse> {
        return try {
            val response = orderApiService.createOrder(
                "Bearer $token",
                CreateOrderRequest(laundryItemId, customerName, weight, notes)
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: OrderResponse(false, "Empty"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        token: String,
        id: Int,
        status: String
    ): Result<OrderResponse> {
        return try {
            val response = orderApiService.updateOrderStatus(
                "Bearer $token", id, UpdateOrderStatusRequest(status)
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: OrderResponse(false, "Empty"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteOrder(token: String, id: Int): Result<BaseResponse> {
        return try {
            val response = orderApiService.deleteOrder("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(response.body() ?: BaseResponse(false, "Empty"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}