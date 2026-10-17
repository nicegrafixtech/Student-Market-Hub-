package com.example.data.repository

import com.example.data.local.MarketplaceDao
import com.example.data.model.*
import com.example.data.order.OrderStatus
import com.example.data.order.OrderStatusStateMachine
import com.example.data.order.OrderSecurityPolicy
import com.example.data.payment.PaystackPaymentVerifier
import com.example.data.payment.ServerPaystackPaymentVerifier
import com.example.data.payment.VerificationResult
import com.example.data.validation.ValidationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MarketplaceRepository(
    private val dao: MarketplaceDao,
    private val paystackVerifier: PaystackPaymentVerifier = ServerPaystackPaymentVerifier()
) {

    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val activeProducts: Flow<List<Product>> = dao.getActiveProducts()
    val allServices: Flow<List<ServiceItem>> = dao.getAllServices()
    val allStores: Flow<List<Store>> = dao.getAllStores()
    val allOrders: Flow<List<Order>> = dao.getAllOrders()
    val allSubscriptions: Flow<List<Subscription>> = dao.getAllSubscriptions()

    suspend fun initSeedDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = dao.getAllProducts().first()
        if (existing.isEmpty()) {
            seedInitialData()
        }
    }

    private suspend fun seedInitialData() {
        val customerProfile = UserProfile(
            id = "user_cust_01",
            email = "nicetech667@gmail.com",
            fullName = "Tunde Adeleke",
            role = UserRole.CUSTOMER,
            campus = "University of Lagos (UNILAG)",
            phone = "+234 812 456 7890",
            matricNumber = "21/0458/ENG",
            department = "Mechanical Engineering"
        )
        dao.insertProfile(customerProfile)

        val sellerStore = Store(
            id = "store_unilag_femzy",
            sellerId = "seller_femzy",
            name = "Femzy Gadgets & Accessories",
            campus = "University of Lagos (UNILAG)",
            description = "Quality laptops, earbuds, powerbanks & phone repair. Fast delivery to all UNILAG hostels!",
            logoEmoji = "📱",
            rating = 4.9f,
            reviewCount = 38,
            isVerified = true,
            whatsappNumber = "+2348039281726",
            hostelLocation = "Jaja Hall, Block A"
        )
        dao.insertStore(sellerStore)

        val sampleProducts = listOf(
            Product(
                id = "prod_01",
                sellerId = "seller_femzy",
                storeId = "store_unilag_femzy",
                storeName = "Femzy Gadgets & Accessories",
                name = "Oraimo FreePods 4 (Active Noise Cancellation)",
                description = "Original Oraimo FreePods with ANC, transparent mode, and 35.5 hrs playtime. Box and receipt intact.",
                price = 28500.0,
                discountPrice = 24900.0,
                category = "Phones & Accessories",
                condition = "Brand New",
                campus = "University of Lagos (UNILAG)",
                quantity = 8,
                iconEmoji = "🎧",
                isFeatured = true
            ),
            Product(
                id = "prod_02",
                sellerId = "seller_femzy",
                storeId = "store_unilag_femzy",
                storeName = "Femzy Gadgets & Accessories",
                name = "Calculus & Linear Algebra 3rd Edition (Textbook)",
                description = "Clean copy used for Engineering Math. No pencil markings inside, all pages complete.",
                price = 6500.0,
                discountPrice = 4500.0,
                category = "Books & Stationery",
                condition = "Like New",
                campus = "University of Lagos (UNILAG)",
                quantity = 3,
                iconEmoji = "📚",
                isFeatured = true
            ),
            Product(
                id = "prod_03",
                sellerId = "seller_femzy",
                storeId = "store_unilag_femzy",
                storeName = "Femzy Gadgets & Accessories",
                name = "Nike Air Force 1 White (Size 43)",
                description = "Clean triple white kicks. Bought last semester, gently used for campus presentations.",
                price = 22000.0,
                discountPrice = 18000.0,
                category = "Shoes & Bags",
                condition = "Like New",
                campus = "University of Lagos (UNILAG)",
                quantity = 1,
                iconEmoji = "👟",
                isFeatured = false
            ),
            Product(
                id = "prod_04",
                sellerId = "seller_femzy",
                storeId = "store_unilag_femzy",
                storeName = "Femzy Gadgets & Accessories",
                name = "20,000mAh Romoss Fast-Charge Power Bank",
                description = "Dual USB + Type C 22.5W output. Charges phone up to 4.5 times during hostel outages.",
                price = 19500.0,
                discountPrice = null,
                category = "Phones & Accessories",
                condition = "Brand New",
                campus = "University of Lagos (UNILAG)",
                quantity = 12,
                iconEmoji = "🔋",
                isFeatured = true
            )
        )
        dao.insertProducts(sampleProducts)

        val sampleServices = listOf(
            ServiceItem(
                id = "serv_01",
                sellerId = "seller_bisi",
                providerName = "Bisi Beauty Hub",
                title = "Knotless Braids & Wig Revamping",
                category = "Beauty & Hair",
                startingPrice = 8000.0,
                description = "Neat campus hairstyling right in Moremi Hall or doorstep hostel service.",
                campus = "University of Lagos (UNILAG)",
                turnaroundTime = "3-4 hrs",
                iconEmoji = "💇‍♀️"
            ),
            ServiceItem(
                id = "serv_02",
                sellerId = "seller_emeka",
                providerName = "Apex Print & Design",
                title = "Final Year Project Binding & Printing",
                category = "Printing & Projects",
                startingPrice = 2500.0,
                description = "Hardcover project binding, color printing, and formatting according to school guidelines.",
                campus = "University of Lagos (UNILAG)",
                turnaroundTime = "24 hrs",
                iconEmoji = "🖨️"
            )
        )
        dao.insertServices(sampleServices)
    }

    suspend fun getProfile(id: String): UserProfile? = dao.getProfile(id)
    suspend fun saveProfile(profile: UserProfile) = dao.insertProfile(profile)
    fun getStoreBySeller(sellerId: String): Flow<Store?> = dao.getStoreBySeller(sellerId)
    suspend fun saveStore(store: Store) = dao.insertStore(store)
    suspend fun updateStoreVerification(storeId: String, isVerified: Boolean) = dao.updateStoreVerification(storeId, isVerified)

    fun getProductsBySeller(sellerId: String): Flow<List<Product>> = dao.getProductsBySeller(sellerId)
    suspend fun insertProduct(product: Product) = dao.insertProduct(product)
    suspend fun updateProduct(product: Product) = dao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)
    suspend fun updateProductStatus(id: String, status: String) = dao.updateProductStatus(id, status)
    suspend fun toggleProductFeatured(id: String, isFeatured: Boolean) = dao.updateProductFeatured(id, isFeatured)

    fun getOrdersByCustomer(customerId: String): Flow<List<Order>> = dao.getOrdersByCustomer(customerId)
    fun getOrdersByStore(storeId: String): Flow<List<Order>> = dao.getOrdersByStore(storeId)

    suspend fun placeOrderWithPaystack(
        customerId: String,
        customerName: String,
        storeId: String,
        storeName: String,
        items: List<Pair<Product, Int>>,
        deliveryCampus: String,
        deliveryHostel: String,
        contactPhone: String,
        paystackRef: String,
        initialStatus: OrderStatus = OrderStatus.PENDING
    ): Order = withContext(Dispatchers.IO) {
        val totalAmount = items.sumOf { (prod, qty) ->
            (prod.discountPrice ?: prod.price) * qty
        } + 500.0

        val orderId = "ord_" + UUID.randomUUID().toString().take(8)
        val order = Order(
            id = orderId,
            customerId = customerId,
            customerName = customerName,
            storeId = storeId,
            storeName = storeName,
            totalAmount = totalAmount,
            status = initialStatus.name,
            paystackRef = paystackRef,
            paymentStatus = if (initialStatus == OrderStatus.PAID) "SUCCESSFUL" else "PENDING",
            deliveryCampus = deliveryCampus,
            deliveryHostel = deliveryHostel,
            contactPhone = contactPhone
        )

        val mockCustomerProfile = UserProfile(
            id = customerId,
            email = "",
            fullName = customerName,
            role = UserRole.CUSTOMER
        )
        OrderSecurityPolicy.validateInsert(order, mockCustomerProfile)

        dao.insertOrder(order)
        val orderItems = items.map { (prod, qty) ->
            OrderItem(
                orderId = orderId,
                productId = prod.id,
                productName = prod.name,
                productPrice = prod.discountPrice ?: prod.price,
                quantity = qty,
                iconEmoji = prod.iconEmoji
            )
        }
        dao.insertOrderItems(orderItems)
        order
    }

    suspend fun updateOrderStatusSecure(
        orderId: String,
        targetStatus: OrderStatus,
        actor: UserProfile,
        sellerStoreId: String? = null,
        reason: String? = null
    ): Result<Order> = withContext(Dispatchers.IO) {
        val existing = dao.getOrderDirect(orderId)
            ?: return@withContext Result.failure(IllegalArgumentException("Order not found: $orderId"))

        try {
            OrderSecurityPolicy.validateUpdate(existing, targetStatus, actor, sellerStoreId)

            if (targetStatus == OrderStatus.CANCELLED) {
                dao.updateOrderStatusWithReason(
                    orderId = orderId,
                    status = targetStatus.name,
                    reason = reason ?: "Cancelled by ${actor.role.name.lowercase()}"
                )
            } else {
                dao.updateOrderStatus(orderId, targetStatus.name)
            }

            val updated = dao.getOrderDirect(orderId)!!
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPendingOrder(
        customerId: String,
        customerName: String,
        storeId: String,
        storeName: String,
        items: List<Pair<Product, Int>>,
        deliveryCampus: String,
        deliveryHostel: String,
        contactPhone: String
    ): Result<Order> = withContext(Dispatchers.IO) {
        val phoneValidation = ValidationEngine.validatePhoneNumber(contactPhone)
        if (!phoneValidation.isValid) {
            return@withContext Result.failure(IllegalArgumentException(phoneValidation.errorMessage))
        }

        val hostelValidation = ValidationEngine.validateHostelLocation(deliveryHostel)
        if (!hostelValidation.isValid) {
            return@withContext Result.failure(IllegalArgumentException(hostelValidation.errorMessage))
        }

        val order = placeOrderWithPaystack(
            customerId = customerId,
            customerName = customerName,
            storeId = storeId,
            storeName = storeName,
            items = items,
            deliveryCampus = deliveryCampus,
            deliveryHostel = deliveryHostel,
            contactPhone = contactPhone,
            paystackRef = "PENDING_" + System.currentTimeMillis().toString().takeLast(6),
            initialStatus = OrderStatus.PENDING
        )
        Result.success(order)
    }

    suspend fun verifyAndFinalizeOrderPayment(
        orderId: String,
        reference: String,
        expectedAmount: Double,
        currency: String = "NGN"
    ): Result<Order> = withContext(Dispatchers.IO) {
        val existing = dao.getOrderDirect(orderId)
            ?: return@withContext Result.failure(IllegalArgumentException("Order not found: $orderId"))

        if (existing.status == OrderStatus.PAID.name || existing.status == OrderStatus.DELIVERED.name) {
            return@withContext Result.success(existing)
        }

        val verification = paystackVerifier.verifyPayment(
            orderId = orderId,
            reference = reference,
            expectedAmount = expectedAmount,
            expectedCurrency = currency
        )

        when (verification) {
            is VerificationResult.Success -> {
                dao.updateOrderStatusWithDetails(
                    orderId = orderId,
                    status = OrderStatus.PAID.name,
                    paymentStatus = "SUCCESSFUL",
                    paystackRef = reference
                )
                val updated = dao.getOrderDirect(orderId)!!
                Result.success(updated)
            }
            is VerificationResult.Failed -> {
                dao.updatePaymentStatus(
                    orderId = orderId,
                    paymentStatus = "FAILED"
                )
                Result.failure(SecurityException("Paystack verification failed: ${verification.reason} (code=${verification.code})"))
            }
            is VerificationResult.NetworkError -> {
                Result.failure(java.io.IOException(verification.message))
            }
        }
    }

    suspend fun processOrderPayment(
        orderId: String,
        paystackRef: String,
        actor: UserProfile
    ): Result<Order> = withContext(Dispatchers.IO) {
        val order = dao.getOrderDirect(orderId)
            ?: return@withContext Result.failure(IllegalArgumentException("Order not found: $orderId"))
        verifyAndFinalizeOrderPayment(
            orderId = orderId,
            reference = paystackRef,
            expectedAmount = order.totalAmount,
            currency = "NGN"
        )
    }

    suspend fun deliverOrder(orderId: String, actor: UserProfile): Result<Order> = withContext(Dispatchers.IO) {
        updateOrderStatusSecure(orderId = orderId, targetStatus = OrderStatus.DELIVERED, actor = actor)
    }

    suspend fun cancelOrder(orderId: String, actor: UserProfile, reason: String): Result<Order> = withContext(Dispatchers.IO) {
        updateOrderStatusSecure(orderId = orderId, targetStatus = OrderStatus.CANCELLED, actor = actor, reason = reason)
    }

    suspend fun recordSellerSubscription(
        sellerId: String,
        storeName: String,
        paystackRef: String
    ): Subscription = withContext(Dispatchers.IO) {
        val sub = Subscription(
            id = "sub_" + UUID.randomUUID().toString().take(8),
            sellerId = sellerId,
            storeName = storeName,
            amount = 1000.0,
            currency = "NGN",
            status = "ACTIVE",
            paystackRef = paystackRef
        )
        dao.insertSubscription(sub)
        sub
    }

    fun getSubscriptionForSeller(sellerId: String): Flow<Subscription?> = dao.getSubscriptionForSeller(sellerId)

    fun getMessages(conversationId: String): Flow<List<Message>> = dao.getMessages(conversationId)
    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        text: String,
        isFromSeller: Boolean
    ) = withContext(Dispatchers.IO) {
        val msg = Message(
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            text = text,
            isFromSeller = isFromSeller
        )
        dao.insertMessage(msg)
    }

    fun getReviewsForProduct(productId: String): Flow<List<Review>> = dao.getReviewsForProduct(productId)
    suspend fun addReview(productId: String, customerName: String, rating: Int, comment: String) = withContext(Dispatchers.IO) {
        val rev = Review(
            id = "rev_" + UUID.randomUUID().toString().take(8),
            productId = productId,
            customerName = customerName,
            rating = rating,
            comment = comment,
            dateText = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        )
        dao.insertReview(rev)
    }
}
