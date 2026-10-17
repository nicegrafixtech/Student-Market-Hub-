package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketplaceDao {

    // Profiles
    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    // Products
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE status = 'ACTIVE' ORDER BY isFeatured DESC, createdAt DESC")
    fun getActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getProductsBySeller(sellerId: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductDirect(id: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET status = :status WHERE id = :id")
    suspend fun updateProductStatus(id: String, status: String)

    @Query("UPDATE products SET isFeatured = :isFeatured WHERE id = :id")
    suspend fun updateProductFeatured(id: String, isFeatured: Boolean)

    // Services
    @Query("SELECT * FROM services ORDER BY completedJobs DESC")
    fun getAllServices(): Flow<List<ServiceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceItem>)

    // Stores
    @Query("SELECT * FROM stores ORDER BY rating DESC")
    fun getAllStores(): Flow<List<Store>>

    @Query("SELECT * FROM stores WHERE sellerId = :sellerId LIMIT 1")
    fun getStoreBySeller(sellerId: String): Flow<Store?>

    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    suspend fun getStoreDirect(id: String): Store?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: Store)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<Store>)

    @Query("UPDATE stores SET isVerified = :isVerified WHERE id = :id")
    suspend fun updateStoreVerification(id: String, isVerified: Boolean)

    // Favorites
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavorites(userId: String): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE userId = :userId AND productId = :productId")
    suspend fun removeFavorite(userId: String, productId: String)

    // Orders
    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderDirect(id: String): Order?

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersByCustomer(customerId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE storeId = :storeId ORDER BY createdAt DESC")
    fun getOrdersByStore(storeId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET status = :status, cancellationReason = :reason, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatusWithReason(orderId: String, status: String, reason: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET status = :status, paymentStatus = :paymentStatus, paystackRef = :paystackRef, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatusWithDetails(orderId: String, status: String, paymentStatus: String, paystackRef: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET paymentStatus = :paymentStatus, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updatePaymentStatus(orderId: String, paymentStatus: String, updatedAt: Long = System.currentTimeMillis())

    // Order Items
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: String): Flow<List<OrderItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    // Subscriptions
    @Query("SELECT * FROM subscriptions WHERE sellerId = :sellerId ORDER BY nextBillingDate DESC LIMIT 1")
    fun getSubscriptionForSeller(sellerId: String): Flow<Subscription?>

    @Query("SELECT * FROM subscriptions ORDER BY paymentDate DESC")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)

    // Messages
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessages(conversationId: String): Flow<List<Message>>

    @Insert
    suspend fun insertMessage(message: Message)

    // Reviews
    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY createdAt DESC")
    fun getReviewsForProduct(productId: String): Flow<List<Review>>

    @Insert
    suspend fun insertReview(review: Review)

    // Notifications
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotifications(userId: String): Flow<List<NotificationItem>>

    @Insert
    suspend fun insertNotification(notification: NotificationItem)
}
