package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    SELLER,
    ADMIN
}

@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val role: UserRole = UserRole.CUSTOMER,
    val campus: String = "University of Lagos (UNILAG)",
    val phone: String = "+234 812 345 6789",
    val avatarUrl: String = "",
    val matricNumber: String = "2023/SCI/0481",
    val department: String = "Computer Science",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stores")
data class Store(
    @PrimaryKey val id: String,
    val sellerId: String,
    val name: String,
    val campus: String,
    val description: String,
    val logoEmoji: String = "🏪",
    val bannerUrl: String = "",
    val rating: Float = 4.8f,
    val reviewCount: Int = 12,
    val isVerified: Boolean = true,
    val whatsappNumber: String = "+2348012345678",
    val hostelLocation: String = "New Hall, Room B12",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "services")
data class ServiceItem(
    @PrimaryKey val id: String,
    val sellerId: String,
    val providerName: String,
    val title: String,
    val category: String,
    val startingPrice: Double,
    val description: String,
    val campus: String,
    val turnaroundTime: String = "24-48 hrs",
    val iconEmoji: String = "⚡",
    val contactPhone: String = "+2348098765432",
    val rating: Float = 4.9f,
    val completedJobs: Int = 24,
    val status: String = "AVAILABLE"
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val productId: String
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val storeId: String,
    val storeName: String,
    val totalAmount: Double,
    val status: String = "PENDING",
    val paystackRef: String,
    val paymentStatus: String = "PENDING",
    val deliveryCampus: String,
    val deliveryHostel: String,
    val contactPhone: String,
    val cancellationReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val iconEmoji: String = "📦"
)

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey val id: String,
    val sellerId: String,
    val storeName: String,
    val amount: Double = 1000.0,
    val currency: String = "NGN",
    val status: String = "ACTIVE",
    val paystackRef: String,
    val paymentDate: Long = System.currentTimeMillis(),
    val nextBillingDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val isFromSeller: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String,
    val productId: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val dateText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String = "ORDER",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
