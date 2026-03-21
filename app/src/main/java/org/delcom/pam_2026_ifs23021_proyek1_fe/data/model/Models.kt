package org.delcom.pam_2026_ifs23021_proyek1_fe.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Auth ───────────────────────────────────────────────────────────────────

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val success: Boolean? = null,         // opsional, tidak semua backend kirim ini
    val message: String? = null,
    val token: String? = null,            // flat format
    val user: User? = null,               // flat format
    val data: AuthData? = null,           // nested format
    val error: String? = null,            // beberapa backend kirim field error
    val status: String? = null            // atau field status
) {
    fun resolveToken(): String? = token ?: data?.token
    fun resolveUser(): User? = user ?: data?.user
    fun resolveSuccess(): Boolean {
        // Cek berbagai cara backend menyatakan sukses
        if (success != null) return success
        if (status != null) return status == "success" || status == "ok"
        if (error != null) return false
        // Jika ada token, berarti berhasil
        return resolveToken() != null
    }
}

@Serializable
data class AuthData(
    val token: String? = null,
    val user: User? = null
)

// ─── User ────────────────────────────────────────────────────────────────────

@Serializable
data class User(
    val id: Int = 0,
    val name: String = "",
    val username: String = "",
    val role: String = "customer"
)

@Serializable
data class UserResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: User? = null
)

// ─── Laundry Item ─────────────────────────────────────────────────────────────

@Serializable
data class LaundryItem(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    @SerialName("price_per_kg") val pricePerKg: Double = 0.0,
    @SerialName("estimated_days") val estimatedDays: Int = 1,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class LaundryItemListResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: LaundryItemData? = null
)

@Serializable
data class LaundryItemData(
    val items: List<LaundryItem> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 10,
    @SerialName("total_pages") val totalPages: Int = 1
)

@Serializable
data class LaundryItemResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: LaundryItem? = null
)

@Serializable
data class CreateLaundryItemRequest(
    val name: String,
    val description: String,
    @SerialName("price_per_kg") val pricePerKg: Double,
    @SerialName("estimated_days") val estimatedDays: Int
)

// ─── Order ───────────────────────────────────────────────────────────────────

@Serializable
data class Order(
    val id: Int = 0,
    @SerialName("user_id") val userId: Int = 0,
    @SerialName("laundry_item_id") val laundryItemId: Int = 0,
    @SerialName("customer_name") val customerName: String = "",
    @SerialName("laundry_item_name") val laundryItemName: String = "",
    val weight: Double = 0.0,
    @SerialName("total_price") val totalPrice: Double = 0.0,
    val status: String = "pending",
    val notes: String = "",
    @SerialName("estimated_done") val estimatedDone: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class OrderListResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: OrderData? = null
)

@Serializable
data class OrderData(
    val orders: List<Order> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 10,
    @SerialName("total_pages") val totalPages: Int = 1
)

@Serializable
data class OrderResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: Order? = null
)

@Serializable
data class CreateOrderRequest(
    @SerialName("laundry_item_id") val laundryItemId: Int,
    @SerialName("customer_name") val customerName: String,
    val weight: Double,
    val notes: String = ""
)

@Serializable
data class UpdateOrderStatusRequest(
    val status: String
)

@Serializable
data class BaseResponse(
    val success: Boolean? = null,
    val message: String? = null
)