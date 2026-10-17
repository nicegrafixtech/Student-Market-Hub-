package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.auth.AuthManager
import com.example.data.firestore.FirestoreProductRepository
import com.example.data.local.AppDatabase
import com.example.data.model.Product
import com.example.data.model.UserRole
import com.example.data.repository.MarketplaceRepository
import com.example.ui.components.PaystackPaymentSheet
import com.example.ui.components.ServiceCard
import com.example.ui.components.StudentAuthDialog
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.customer.*
import com.example.ui.screens.seller.AddEditProductDialog
import com.example.ui.screens.seller.ListNewProductScreen
import com.example.ui.screens.seller.SellerDashboardScreen
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CustomerTab
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.ui.viewmodel.PaystackSheetType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val database = remember { AppDatabase.getDatabase(context) }
                val authManager = remember { AuthManager(context) }
                val firestoreProductRepository = remember {
                    try {
                        FirestoreProductRepository(context)
                    } catch (e: Exception) {
                        null
                    }
                }
                val repository = remember { MarketplaceRepository(database.marketplaceDao()) }

                val viewModel: MarketplaceViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return MarketplaceViewModel(repository, authManager, firestoreProductRepository) as T
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    authManager.attemptAutoSignIn(
                        activityContext = context,
                        scope = scope,
                        onAuthSuccess = { viewModel.syncAuthenticatedGoogleUser() }
                    )
                }

                MarketplaceApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceApp(viewModel: MarketplaceViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCampus by viewModel.selectedCampus.collectAsStateWithLifecycle()

    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle(emptyList())
    val allServices by viewModel.allServices.collectAsStateWithLifecycle(emptyList())
    val allStores by viewModel.allStores.collectAsStateWithLifecycle(emptyList())
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val customerOrders by viewModel.customerOrders.collectAsStateWithLifecycle()

    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val selectedProductReviews by viewModel.selectedProductReviews.collectAsStateWithLifecycle()

    val sellerStore by viewModel.sellerStore.collectAsStateWithLifecycle()
    val sellerProducts by viewModel.sellerProducts.collectAsStateWithLifecycle()
    val sellerOrders by viewModel.sellerOrders.collectAsStateWithLifecycle()
    val sellerSubscription by viewModel.sellerSubscription.collectAsStateWithLifecycle()

    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle(emptyList())
    val allSubscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle(emptyList())
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()

    val paystackSheetState by viewModel.paystackSheetState.collectAsStateWithLifecycle()

    var customerTab by remember { mutableStateOf(CustomerTab.MARKETPLACE) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showListNewProductScreen by remember { mutableStateOf(false) }
    var showAddEditProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    if (showListNewProductScreen) {
        ListNewProductScreen(
            defaultCampus = selectedCampus,
            onPublishProduct = { title, desc, price, discount, imageUrl, cat, cond, qty, campus, emoji ->
                viewModel.addProduct(
                    name = title,
                    description = desc,
                    price = price,
                    discountPrice = discount,
                    category = cat,
                    condition = cond,
                    quantity = qty,
                    campus = campus,
                    emoji = emoji,
                    imageUrl = imageUrl
                )
                showListNewProductScreen = false
                Toast.makeText(context, "Product listed on marketplace!", Toast.LENGTH_SHORT).show()
            },
            onBack = { showListNewProductScreen = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text("🇳🇬", modifier = Modifier.padding(end = 6.dp))
                        Text(
                            text = when (userProfile.role) {
                                UserRole.CUSTOMER -> "Student Market Hub"
                                UserRole.SELLER -> "Seller Command Hub"
                                UserRole.ADMIN -> "Super Admin Moderator"
                            },
                            color = BrandNavy
                        )
                    }
                },
                actions = {
                    if (userProfile.role == UserRole.CUSTOMER) {
                        BadgedBox(
                            badge = {
                                val totalItems = cart.values.sum()
                                if (totalItems > 0) {
                                    Badge(containerColor = BrandOrange) {
                                        Text(totalItems.toString())
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { customerTab = CustomerTab.CART }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = BrandNavy)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (userProfile.role == UserRole.CUSTOMER && customerTab == CustomerTab.MARKETPLACE) {
                ExtendedFloatingActionButton(
                    onClick = { showListNewProductScreen = true },
                    containerColor = BrandOrange,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = "List Item for Sale") },
                    text = { Text("Sell Item", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("open_list_product_screen_fab")
                )
            }
        },
        bottomBar = {
            if (userProfile.role == UserRole.CUSTOMER) {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = customerTab == CustomerTab.MARKETPLACE,
                        onClick = { customerTab = CustomerTab.MARKETPLACE },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Market") },
                        label = { Text("Market") }
                    )
                    NavigationBarItem(
                        selected = customerTab == CustomerTab.SERVICES,
                        onClick = { customerTab = CustomerTab.SERVICES },
                        icon = { Icon(Icons.Default.Bolt, contentDescription = "Services") },
                        label = { Text("Services") }
                    )
                    NavigationBarItem(
                        selected = customerTab == CustomerTab.ORDERS,
                        onClick = { customerTab = CustomerTab.ORDERS },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Orders") },
                        label = { Text("Orders") }
                    )
                    NavigationBarItem(
                        selected = customerTab == CustomerTab.MESSAGES,
                        onClick = { customerTab = CustomerTab.MESSAGES },
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                        label = { Text("Chat") }
                    )
                    NavigationBarItem(
                        selected = customerTab == CustomerTab.PROFILE,
                        onClick = { customerTab = CustomerTab.PROFILE },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            } else {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text(if (userProfile.role == UserRole.SELLER) "Seller Hub" else "Admin") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.switchRole(UserRole.CUSTOMER) },
                        icon = { Icon(Icons.Default.ArrowBack, contentDescription = "Back to Buyer") },
                        label = { Text("Buyer View") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (userProfile.role) {
                UserRole.CUSTOMER -> {
                    when (customerTab) {
                        CustomerTab.MARKETPLACE -> {
                            MarketplaceHomeScreen(
                                products = filteredProducts,
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                selectedCategory = selectedCategory,
                                onCategorySelect = { viewModel.selectCategory(it) },
                                selectedCampus = selectedCampus,
                                onCampusSelect = { viewModel.selectCampus(it) },
                                onProductClick = { viewModel.viewProductDetails(it) },
                                onAddToCart = {
                                    viewModel.addToCart(it)
                                    Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                                },
                                onOpenServices = { customerTab = CustomerTab.SERVICES }
                            )
                        }
                        CustomerTab.SERVICES -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    Text("Campus Student Services", fontSize = 18.sp, color = BrandNavy)
                                }
                                items(allServices, key = { it.id }) { s ->
                                    ServiceCard(
                                        service = s,
                                        onContact = { customerTab = CustomerTab.MESSAGES }
                                    )
                                }
                            }
                        }
                        CustomerTab.CART -> {
                            val cartItems = cart.mapNotNull { (id, qty) ->
                                val p = allProducts.find { it.id == id }
                                if (p != null) p to qty else null
                            }
                            CartCheckoutScreen(
                                cartItems = cartItems,
                                onIncreaseQty = { viewModel.addToCart(it) },
                                onDecreaseQty = { viewModel.removeFromCart(it) },
                                onCheckout = { hostel, campus, phone ->
                                    viewModel.showPaystackForCartCheckout(hostel, campus, phone)
                                },
                                onBrowseProducts = { customerTab = CustomerTab.MARKETPLACE }
                            )
                        }
                        CustomerTab.ORDERS -> {
                            CustomerOrdersScreen(
                                orders = customerOrders,
                                onConfirmDelivery = { orderId ->
                                    viewModel.updateOrderStatus(orderId, com.example.data.order.OrderStatus.DELIVERED)
                                    Toast.makeText(context, "Delivery confirmed! Merchant credited.", Toast.LENGTH_SHORT).show()
                                },
                                onCancelOrder = { orderId ->
                                    viewModel.updateOrderStatus(orderId, com.example.data.order.OrderStatus.CANCELLED, "Cancelled by buyer")
                                }
                            )
                        }
                        CustomerTab.MESSAGES -> {
                            MessagesScreen(
                                messages = currentMessages,
                                onSendMessage = { text, isSeller -> viewModel.sendMessage(text, isSeller) }
                            )
                        }
                        CustomerTab.PROFILE -> {
                            CustomerProfileScreen(
                                profile = userProfile,
                                onRoleChange = { viewModel.switchRole(it) },
                                onOpenAuthDialog = { showAuthDialog = true },
                                onSignOut = {
                                    viewModel.authManager.signOut(scope) {
                                        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
                UserRole.SELLER -> {
                    SellerDashboardScreen(
                        store = sellerStore,
                        products = sellerProducts,
                        orders = sellerOrders,
                        subscription = sellerSubscription,
                        onAddProductClick = {
                            showListNewProductScreen = true
                        },
                        onEditProductClick = {
                            productToEdit = it
                            showAddEditProductDialog = true
                        },
                        onDeleteProductClick = {
                            viewModel.deleteProduct(it)
                            Toast.makeText(context, "Listing deleted", Toast.LENGTH_SHORT).show()
                        },
                        onUpdateOrderStatus = { id, status, reason ->
                            viewModel.updateOrderStatus(id, status, reason)
                        },
                        onRenewSubscription = { viewModel.showPaystackForSellerSubscription() },
                        onSaveStoreDetails = { viewModel.saveStoreDetails(it) }
                    )
                }
                UserRole.ADMIN -> {
                    AdminDashboardScreen(
                        products = allProducts,
                        stores = allStores,
                        orders = allOrders,
                        subscriptions = allSubscriptions,
                        onApproveProduct = { viewModel.approveProduct(it) },
                        onRejectProduct = { viewModel.rejectProduct(it) },
                        onToggleFeature = { id, cur -> viewModel.toggleProductFeature(id, cur) },
                        onDeleteProduct = {
                            val p = allProducts.find { prod -> prod.id == it }
                            if (p != null) viewModel.deleteProduct(p)
                        },
                        onToggleStoreVerification = { id, cur -> viewModel.toggleStoreVerification(id, cur) }
                    )
                }
            }

            // Product Details Bottom Sheet
            selectedProduct?.let { prod ->
                ProductDetailSheet(
                    product = prod,
                    reviews = selectedProductReviews,
                    onAddToCart = {
                        viewModel.addToCart(prod.id)
                        Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                    },
                    onContactSeller = {
                        viewModel.viewProductDetails(null)
                        customerTab = CustomerTab.MESSAGES
                    },
                    onSubmitReview = { r, c -> viewModel.submitReview(r, c) },
                    onDismiss = { viewModel.viewProductDetails(null) }
                )
            }

            // Paystack Payment Sheet Modal
            paystackSheetState?.let { sheetType ->
                val amount = when (sheetType) {
                    is PaystackSheetType.CartCheckout -> sheetType.expectedAmount
                    is PaystackSheetType.SellerSubscription -> sheetType.expectedAmount
                }

                PaystackPaymentSheet(
                    amount = amount,
                    customerEmail = userProfile.email,
                    title = when (sheetType) {
                        is PaystackSheetType.CartCheckout -> "Checkout Campus Order"
                        is PaystackSheetType.SellerSubscription -> "Seller Monthly Subscription"
                    },
                    subtitle = when (sheetType) {
                        is PaystackSheetType.CartCheckout -> "Hostel doorstep delivery • Student Market Hub"
                        is PaystackSheetType.SellerSubscription -> "₦1,000/month • Unlimited store listings"
                    },
                    onVerifyAndProcess = { ref, onResult ->
                        viewModel.executePaystackPayment(ref, sheetType) { success, errorMsg ->
                            onResult(success, errorMsg)
                            if (success) {
                                Toast.makeText(context, "Payment verified & order placed!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onDismiss = { viewModel.dismissPaystack() }
                )
            }

            // Add / Edit Product Dialog
            if (showAddEditProductDialog) {
                AddEditProductDialog(
                    initialProduct = productToEdit,
                    defaultCampus = selectedCampus,
                    onSave = { name, desc, price, discount, imageUrl, cat, cond, qty, campus, emoji ->
                        if (productToEdit == null) {
                            viewModel.addProduct(name, desc, price, discount, cat, cond, qty, campus, emoji, imageUrl)
                            Toast.makeText(context, "Product listed on marketplace!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateProduct(
                                productToEdit!!.copy(
                                    name = name,
                                    title = name,
                                    description = desc,
                                    price = price,
                                    discountPrice = discount,
                                    imageUrl = imageUrl,
                                    category = cat,
                                    condition = cond,
                                    quantity = qty,
                                    campus = campus,
                                    iconEmoji = emoji
                                )
                            )
                            Toast.makeText(context, "Listing updated!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDismiss = { showAddEditProductDialog = false }
                )
            }

            // Auth Dialog (Google Sign-In via Credential Manager)
            StudentAuthDialog(
                isOpen = showAuthDialog,
                authManager = viewModel.authManager,
                onDismiss = { showAuthDialog = false },
                onAuthSuccess = {
                    viewModel.syncAuthenticatedGoogleUser()
                    Toast.makeText(context, "Signed in with Google!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
