package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val sellerId: String = "seller_femzy",
    val storeId: String = "store_unilag_femzy",
    val storeName: String = "Femzy Gadgets & Accessories",
    val name: String = "",
    val title: String = name,
    val description: String,
    val price: Double,
    val discountPrice: Double? = null,
    val category: String = "Phones & Accessories",
    val condition: String = "Brand New",
    val campus: String = "University of Lagos (UNILAG)",
    val quantity: Int = 1,
    val iconEmoji: String = "📦",
    val imageUrl: String = "",
    val isFeatured: Boolean = false,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)
