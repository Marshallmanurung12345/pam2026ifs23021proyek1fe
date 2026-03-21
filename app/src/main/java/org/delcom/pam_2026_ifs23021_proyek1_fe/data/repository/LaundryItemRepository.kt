package org.delcom.pam_2026_ifs23021_proyek1_fe.data.repository

import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api.LaundryItemApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LaundryItemRepository @Inject constructor(
    private val laundryItemApiService: LaundryItemApiService
) {
    suspend fun getLaundryItems(
        token: String,
        page: Int = 1,
        perPage: Int = 10,
        search: String? = null
    ): Result<LaundryItemListResponse> {
        return try {
            val response = laundryItemApiService.getLaundryItems(
                "Bearer $token", page, perPage, search?.ifBlank { null }
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: LaundryItemListResponse(false, "Empty"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLaundryItem(token: String, id: Int): Result<LaundryItemResponse> {
        return try {
            val response = laundryItemApiService.getLaundryItem("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(response.body() ?: LaundryItemResponse(false, "Empty"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createLaundryItem(
        token: String,
        name: String,
        description: String,
        pricePerKg: Double,
        estimatedDays: Int
    ): Result<LaundryItemResponse> {
        return try {
            val response = laundryItemApiService.createLaundryItem(
                "Bearer $token",
                CreateLaundryItemRequest(name, description, pricePerKg, estimatedDays)
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: LaundryItemResponse(false, "Empty"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLaundryItem(
        token: String,
        id: Int,
        name: String,
        description: String,
        pricePerKg: Double,
        estimatedDays: Int
    ): Result<LaundryItemResponse> {
        return try {
            val response = laundryItemApiService.updateLaundryItem(
                "Bearer $token", id,
                CreateLaundryItemRequest(name, description, pricePerKg, estimatedDays)
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: LaundryItemResponse(false, "Empty"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLaundryItem(token: String, id: Int): Result<BaseResponse> {
        return try {
            val response = laundryItemApiService.deleteLaundryItem("Bearer $token", id)
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