package org.delcom.pam_2026_ifs23021_proyek1_fe.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Auth ────────────────────────────────────────────────────────────────────
// POST /auth/login  → body: {username, password}
// POST /auth/register → body: {name, username, password}
// Response: { status: "success", message: "...", data: { authToken: "...", refreshToken: "..." } }

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
    val status: String? = null,
    val message: String? = null,
    val data: AuthTokenData? = null
)

@Serializable
data class AuthTokenData(
    val authToken: String? = null,
    val refreshToken: String? = null,
    val userId: String? = null
)

// ─── User ─────────────────────────────────────────────────────────────────────
// GET /users/me → { status, message, data: { user: {...} } }
@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val photo: String? = null,
    val urlPhoto: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class UserMeResponse(
    val status: String? = null,
    val message: String? = null,
    val data: UserMeData? = null
)

@Serializable
data class UserMeData(
    val user: User? = null
)

// ─── Laundry Service (Jenis Layanan) ─────────────────────────────────────────
// GET /laundry-services → { status, message, data: { laundryServices: [...] } }
// GET /laundry-services/{id} → { status, message, data: { laundryService: {...} } }
@Serializable
data class LaundryService(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val unit: String = "",
    val estimatedDays: Int = 1,
    val image: String? = null,
    val urlImage: String = "",
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class LaundryServiceListResponse(
    val status: String? = null,
    val message: String? = null,
    val data: LaundryServiceListData? = null
)

@Serializable
data class LaundryServiceListData(
    val laundryServices: List<LaundryService> = emptyList()
)

@Serializable
data class LaundryServiceResponse(
    val status: String? = null,
    val message: String? = null,
    val data: LaundryServiceData? = null
)

@Serializable
data class LaundryServiceData(
    val laundryService: LaundryService? = null
)

@Serializable
data class CreateLaundryServiceRequest(
    val name: String,
    val description: String,
    val price: Double,
    val unit: String,
    val estimatedDays: Int,
    val isActive: Boolean = true
)

// ─── Laundry Order ────────────────────────────────────────────────────────────
// GET /laundry-orders → { status, message, data: { laundryOrders: [...], pagination: {...} } }
@Serializable
data class LaundryOrder(
    val id: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val quantity: Double = 0.0,
    val totalPrice: Double = 0.0,
    val status: String = "pending",
    val notes: String? = null,
    val pickupDate: String? = null,
    val deliveryDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class LaundryOrderListResponse(
    val status: String? = null,
    val message: String? = null,
    val data: LaundryOrderListData? = null
)

@Serializable
data class LaundryOrderListData(
    val laundryOrders: List<LaundryOrder> = emptyList(),
    val pagination: Pagination? = null
)

@Serializable
data class Pagination(
    val page: Int = 1,
    val limit: Int = 10,
    val total: Int = 0,
    val totalPages: Int = 1,
    val hasNext: Boolean = false
)

@Serializable
data class LaundryOrderResponse(
    val status: String? = null,
    val message: String? = null,
    val data: LaundryOrderData? = null
)

@Serializable
data class LaundryOrderData(
    val laundryOrder: LaundryOrder? = null
)

@Serializable
data class CreateOrderRequest(
    val serviceId: String,
    val customerName: String,
    val customerPhone: String,
    val quantity: Double,
    val totalPrice: Double,
    val notes: String? = null
)

@Serializable
data class UpdateOrderRequest(
    val serviceId: String,
    val customerName: String,
    val customerPhone: String,
    val quantity: Double,
    val totalPrice: Double,
    val notes: String? = null
)

@Serializable
data class UpdateOrderStatusRequest(
    val status: String
)

@Serializable
data class BaseResponse(
    val status: String? = null,
    val message: String? = null
)