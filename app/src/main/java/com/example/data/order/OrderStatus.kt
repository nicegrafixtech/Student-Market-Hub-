package com.example.data.order

enum class OrderStatus(val displayName: String, val badgeColorHex: Long) {
    PENDING("Pending Payment", 0xFFF59E0B),
    PAID("Paid & Confirmed", 0xFF00C3F7),
    PROCESSING("Processing in Store", 0xFF6366F1),
    DISPATCHED("Dispatched to Hostel", 0xFF8B5CF6),
    DELIVERED("Delivered & Completed", 0xFF10B981),
    CANCELLED("Cancelled", 0xFFEF4444);

    companion object {
        fun fromString(value: String): OrderStatus {
            return when (value.uppercase()) {
                "PENDING" -> PENDING
                "PAID", "CONFIRMED" -> PAID
                "PROCESSING" -> PROCESSING
                "DISPATCHED" -> DISPATCHED
                "DELIVERED" -> DELIVERED
                "CANCELLED" -> CANCELLED
                else -> PENDING
            }
        }
    }
}
