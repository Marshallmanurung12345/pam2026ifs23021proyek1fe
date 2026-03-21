package org.delcom.pam_2026_ifs23021_proyek1_fe.data.remote.api

import org.delcom.pam_2026_ifs23021_proyek1_fe.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}

interface UserApiService {
    @GET("api/users/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserResponse>

    @PUT("api/users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body user: User
    ): Response<UserResponse>
}

interface LaundryItemApiService {
    @GET("api/laundry-items")
    suspend fun getLaundryItems(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("search") search: String? = null
    ): Response<LaundryItemListResponse>

    @GET("api/laundry-items/{id}")
    suspend fun getLaundryItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<LaundryItemResponse>

    @POST("api/laundry-items")
    suspend fun createLaundryItem(
        @Header("Authorization") token: String,
        @Body request: CreateLaundryItemRequest
    ): Response<LaundryItemResponse>

    @PUT("api/laundry-items/{id}")
    suspend fun updateLaundryItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateLaundryItemRequest
    ): Response<LaundryItemResponse>

    @DELETE("api/laundry-items/{id}")
    suspend fun deleteLaundryItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<BaseResponse>
}

interface OrderApiService {
    @GET("api/orders")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<OrderListResponse>

    @GET("api/orders/{id}")
    suspend fun getOrder(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<OrderResponse>

    @POST("api/orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): Response<OrderResponse>

    @PUT("api/orders/{id}")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateOrderStatusRequest
    ): Response<OrderResponse>

    @DELETE("api/orders/{id}")
    suspend fun deleteOrder(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<BaseResponse>
}