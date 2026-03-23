package org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api

import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}

interface UserApiService {
    @GET("users/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<UserMeResponse>

    @PUT("users/me")
    suspend fun updateMe(
        @Header("Authorization") token: String,
        @Body request: Map<String, String>
    ): Response<BaseResponse>
}

interface LaundryServiceApiService {
    @GET("laundry-services")
    suspend fun getAll(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("isActive") isActive: Boolean? = null
    ): Response<LaundryServiceListResponse>

    @GET("laundry-services/{id}")
    suspend fun getById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<LaundryServiceResponse>

    @POST("laundry-services")
    suspend fun create(
        @Header("Authorization") token: String,
        @Body request: CreateLaundryServiceRequest
    ): Response<BaseResponse>

    @PUT("laundry-services/{id}")
    suspend fun update(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: CreateLaundryServiceRequest
    ): Response<BaseResponse>

    @DELETE("laundry-services/{id}")
    suspend fun delete(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<BaseResponse>
}

interface LaundryOrderApiService {
    @GET("laundry-orders")
    suspend fun getAll(
        @Header("Authorization") token: String,
        @Query("search") search: String?,
        @Query("status") status: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<LaundryOrderListResponse>

    @GET("laundry-orders/{id}")
    suspend fun getById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<LaundryOrderResponse>

    @POST("laundry-orders")
    suspend fun create(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): Response<BaseResponse>

    @PUT("laundry-orders/{id}")
    suspend fun update(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateOrderRequest
    ): Response<BaseResponse>

    @PUT("laundry-orders/{id}/status")
    suspend fun updateStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateOrderStatusRequest
    ): Response<BaseResponse>

    @DELETE("laundry-orders/{id}")
    suspend fun delete(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<BaseResponse>
}