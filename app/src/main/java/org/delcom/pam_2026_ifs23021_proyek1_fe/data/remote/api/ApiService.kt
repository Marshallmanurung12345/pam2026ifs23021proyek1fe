package org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api

import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    // Coba salah satu endpoint ini sesuai backend kamu:
    // Option 1: /api/auth/login  (paling umum)
    // Option 2: /auth/login
    // Option 3: /api/users/login
    // Option 4: /login

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}

interface UserApiService {
    @GET("users/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserResponse>

    @PUT("users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body user: User
    ): Response<UserResponse>
}

interface LaundryItemApiService {
    @GET("laundry-items")
    suspend fun getLaundryItems(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("search") search: String? = null
    ): Response<LaundryItemListResponse>

    @GET("laundry-items/{id}")
    suspend fun getLaundryItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<LaundryItemResponse>

    @POST("laundry-items")
    suspend fun createLaundryItem(
        @Header("Authorization") token: String,
        @Body request: CreateLaundryItemRequest
    ): Response<LaundryItemResponse>

    @PUT("laundry-items/{id}")
    suspend fun updateLaundryItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateLaundryItemRequest
    ): Response<LaundryItemResponse>

    @DELETE("laundry-items/{id}")
    suspend fun deleteLaundryItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<BaseResponse>
}

interface OrderApiService {
    @GET("orders")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<OrderListResponse>

    @GET("orders/{id}")
    suspend fun getOrder(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<OrderResponse>

    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): Response<OrderResponse>

    @PUT("orders/{id}")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateOrderStatusRequest
    ): Response<OrderResponse>

    @DELETE("orders/{id}")
    suspend fun deleteOrder(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<BaseResponse>
}