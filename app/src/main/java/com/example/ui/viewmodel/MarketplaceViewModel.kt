package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.model.*
import com.example.data.order.OrderStatus
import com.example.data.repository.MarketplaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class MarketplaceViewModel(
    private val repository: MarketplaceRepository,
    val authManager: AuthManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.initSeedDataIfEmpty()
        }
    }

    // Current Student Profile
    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "user_cust_01",
            email = "nicetech667@gmail.com",
            fullName = "Tunde Adeleke",
            role = UserRole.CUSTOMER,
            campus = "University of Lagos (UNILAG)",
            phone = "+234 812 456 7890",
            matricNumber = "21/0458/ENG",
            department = "Mechanical Engineering"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun switchRole(newRole: UserRole) {
        _userProfile.value = _userProfile.value.copy(role = newRole)
    }

    // Search and Category Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    private val _selectedCampus = MutableStateFlow("All Campuses")
    val selectedCampus: StateFlow<String> = _selectedCampus.asStateFlow()

    fun selectCampus(campus: String) {
        _selectedCampus.value = campus
    }

    // Data streams
    val allProducts = repository.allProducts
    val activeProducts = repository.activeProducts
    val allServices = repository.allServices
    val allStores = repository.allStores

    // Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(
        repository.activeProducts,
        _searchQuery,
        _selectedCategory,
        _selectedCampus
    ) { products, query, category, campus ->
        products.filter { p ->
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.description.contains(query, ignoreCase = true) ||
                    p.category.contains(query, ignoreCase = true) ||
                    p.storeName.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || p.category.equals(category, ignoreCase = true)
            val matchesCampus = campus == "All Campuses" || p.campus.equals(campus, ignoreCase = true)

            matchesQuery && matchesCategory && matchesCampus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart
    private val _cart = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cart: StateFlow<Map<String, Int>> = _cart.asStateFlow()

    fun addToCart(productId: String) {
        val current = _cart.value.toMutableMap()
        current[productId] = (current[productId] ?: 0) + 1
        _cart.value = current
    }

    fun removeFromCart(productId: String) {
        val current = _cart.value.toMutableMap()
        val count = current[productId] ?: 0
        if (count > 1) {
            current[productId] = count - 1
        } else {
            current.remove(productId)
        }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    // Customer Orders
    val customerOrders: StateFlow<List<Order>> = repository.getOrdersByCustomer("user_cust_01")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product Details
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    fun viewProductDetails(product: Product?) {
        _selectedProduct.value = product
    }

    val selectedProductReviews: StateFlow<List<Review>> = _selectedProduct
        .flatMapLatest { prod ->
            if (prod != null) repository.getReviewsForProduct(prod.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitReview(rating: Int, comment: String) {
        val prod = _selectedProduct.value ?: return
        viewModelScope.launch {
            repository.addReview(
                productId = prod.id,
                customerName = userProfile.value.fullName,
                rating = rating,
                comment = comment
            )
        }
    }

    // Seller Hub State
    val sellerStore: StateFlow<Store?> = repository.getStoreBySeller("seller_femzy")
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Store(
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
        )

    val sellerProducts: StateFlow<List<Product>> = repository.getProductsBySeller("seller_femzy")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sellerOrders: StateFlow<List<Order>> = repository.getOrdersByStore("store_unilag_femzy")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sellerSubscription: StateFlow<Subscription?> = repository.getSubscriptionForSeller("seller_femzy")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addProduct(
        name: String,
        description: String,
        price: Double,
        discountPrice: Double?,
        category: String,
        condition: String,
        quantity: Int,
        campus: String,
        emoji: String
    ) {
        viewModelScope.launch {
            val store = sellerStore.value
            val prod = Product(
                id = "prod_" + UUID.randomUUID().toString().take(8),
                sellerId = "seller_femzy",
                storeId = store?.id ?: "store_unilag_femzy",
                storeName = store?.name ?: "Femzy Gadgets",
                name = name,
                description = description,
                price = price,
                discountPrice = discountPrice,
                category = category,
                condition = condition,
                quantity = quantity,
                campus = campus,
                iconEmoji = emoji
            )
            repository.insertProduct(prod)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus, reason: String? = null) {
        viewModelScope.launch {
            repository.updateOrderStatusSecure(
                orderId = orderId,
                targetStatus = newStatus,
                actor = userProfile.value,
                sellerStoreId = "store_unilag_femzy",
                reason = reason
            )
        }
    }

    fun saveStoreDetails(store: Store) {
        viewModelScope.launch {
            repository.saveStore(store)
        }
    }

    // Admin Operations
    val allOrders = repository.allOrders
    val allSubscriptions = repository.allSubscriptions

    fun approveProduct(id: String) {
        viewModelScope.launch { repository.updateProductStatus(id, "ACTIVE") }
    }

    fun rejectProduct(id: String) {
        viewModelScope.launch { repository.updateProductStatus(id, "REJECTED") }
    }

    fun toggleProductFeature(id: String, current: Boolean) {
        viewModelScope.launch { repository.toggleProductFeatured(id, !current) }
    }

    fun toggleStoreVerification(storeId: String, current: Boolean) {
        viewModelScope.launch { repository.updateStoreVerification(storeId, !current) }
    }

    // Paystack Checkout & Subscription Sheets
    private val _paystackSheetState = MutableStateFlow<PaystackSheetType?>(null)
    val paystackSheetState: StateFlow<PaystackSheetType?> = _paystackSheetState.asStateFlow()

    fun showPaystackForCartCheckout(deliveryHostel: String, deliveryCampus: String, phone: String) {
        viewModelScope.launch {
            val productsList = repository.allProducts.first()
            val cartMap = _cart.value
            val cartItems = cartMap.mapNotNull { (id, qty) ->
                val p = productsList.find { it.id == id }
                if (p != null) p to qty else null
            }
            if (cartItems.isEmpty()) return@launch

            val firstProduct = cartItems.first().first
            val subtotal = cartItems.sumOf { (p, q) -> (p.discountPrice ?: p.price) * q }
            val totalAmount = subtotal + 500.0

            val result = repository.createPendingOrder(
                customerId = userProfile.value.id,
                customerName = userProfile.value.fullName,
                storeId = firstProduct.storeId,
                storeName = firstProduct.storeName,
                items = cartItems,
                deliveryCampus = deliveryCampus,
                deliveryHostel = deliveryHostel,
                contactPhone = phone
            )

            result.onSuccess { pendingOrder ->
                _paystackSheetState.value = PaystackSheetType.CartCheckout(
                    pendingOrderId = pendingOrder.id,
                    deliveryHostel = deliveryHostel,
                    deliveryCampus = deliveryCampus,
                    phone = phone,
                    expectedAmount = totalAmount
                )
            }
        }
    }

    fun showPaystackForSellerSubscription() {
        _paystackSheetState.value = PaystackSheetType.SellerSubscription(expectedAmount = 1000.0)
    }

    fun dismissPaystack() {
        _paystackSheetState.value = null
    }

    fun executePaystackPayment(
        reference: String,
        type: PaystackSheetType,
        onResult: (isSuccess: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            when (type) {
                is PaystackSheetType.CartCheckout -> {
                    val result = repository.verifyAndFinalizeOrderPayment(
                        orderId = type.pendingOrderId,
                        reference = reference,
                        expectedAmount = type.expectedAmount,
                        currency = "NGN"
                    )
                    result.fold(
                        onSuccess = {
                            clearCart()
                            onResult(true, null)
                        },
                        onFailure = { error ->
                            onResult(false, error.message ?: "Server-side Paystack verification failed.")
                        }
                    )
                }
                is PaystackSheetType.SellerSubscription -> {
                    val store = sellerStore.value
                    repository.recordSellerSubscription(
                        sellerId = "seller_femzy",
                        storeName = store?.name ?: "Femzy Gadgets & Accessories",
                        paystackRef = reference
                    )
                    onResult(true, null)
                }
            }
        }
    }

    // Messages
    val currentMessages: StateFlow<List<Message>> = repository.getMessages("conv_buyer_seller_1")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(text: String, isFromSeller: Boolean) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = "conv_buyer_seller_1",
                senderId = if (isFromSeller) "seller_femzy" else userProfile.value.id,
                senderName = if (isFromSeller) (sellerStore.value?.name ?: "Store Support") else userProfile.value.fullName,
                text = text,
                isFromSeller = isFromSeller
            )
        }
    }
}

sealed class PaystackSheetType {
    data class CartCheckout(
        val pendingOrderId: String,
        val deliveryHostel: String,
        val deliveryCampus: String,
        val phone: String,
        val expectedAmount: Double
    ) : PaystackSheetType()

    data class SellerSubscription(
        val expectedAmount: Double = 1000.0
    ) : PaystackSheetType()
}

enum class CustomerTab {
    MARKETPLACE,
    SERVICES,
    CART,
    ORDERS,
    MESSAGES,
    PROFILE
}
