package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.order.OrderSecurityPolicy
import com.example.data.order.OrderStatus
import com.example.data.order.OrderStatusStateMachine
import com.example.data.order.RlsPolicyViolationException
import com.example.data.repository.MarketplaceRepository
import com.example.data.validation.ValidationEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: MarketplaceRepository

    private val customerUser = UserProfile(
        id = "cust_tunde",
        email = "tunde@unilag.edu.ng",
        fullName = "Tunde Adeleke",
        role = UserRole.CUSTOMER
    )

    private val otherCustomer = UserProfile(
        id = "cust_chidi",
        email = "chidi@unilag.edu.ng",
        fullName = "Chidi Okafor",
        role = UserRole.CUSTOMER
    )

    private val sellerUser = UserProfile(
        id = "seller_femzy",
        email = "femzy@gadgets.ng",
        fullName = "Femi Gadgets",
        role = UserRole.SELLER
    )

    private val adminUser = UserProfile(
        id = "admin_super",
        email = "admin@studentmarkethub.ng",
        fullName = "Admin Mod",
        role = UserRole.ADMIN
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MarketplaceRepository(database.marketplaceDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Student Market Hub", appName)
    }

    @Test
    fun `order state machine enforces valid and invalid transitions`() {
        assertTrue(OrderStatusStateMachine.canTransition(OrderStatus.PENDING, OrderStatus.PAID, UserRole.CUSTOMER))
        assertTrue(OrderStatusStateMachine.canTransition(OrderStatus.PENDING, OrderStatus.CANCELLED, UserRole.CUSTOMER))

        assertTrue(OrderStatusStateMachine.canTransition(OrderStatus.PAID, OrderStatus.PROCESSING, UserRole.SELLER))
        assertFalse(OrderStatusStateMachine.canTransition(OrderStatus.PAID, OrderStatus.PROCESSING, UserRole.CUSTOMER))

        assertTrue(OrderStatusStateMachine.canTransition(OrderStatus.PROCESSING, OrderStatus.DISPATCHED, UserRole.SELLER))
        assertFalse(OrderStatusStateMachine.canTransition(OrderStatus.PROCESSING, OrderStatus.DISPATCHED, UserRole.CUSTOMER))

        assertTrue(OrderStatusStateMachine.canTransition(OrderStatus.DISPATCHED, OrderStatus.DELIVERED, UserRole.CUSTOMER))
        assertFalse(OrderStatusStateMachine.canTransition(OrderStatus.DELIVERED, OrderStatus.CANCELLED, UserRole.CUSTOMER))
        assertFalse(OrderStatusStateMachine.canTransition(OrderStatus.CANCELLED, OrderStatus.PAID, UserRole.CUSTOMER))
    }

    @Test
    fun `RLS policy enforces order access boundaries`() {
        val order = Order(
            id = "ord_unilag_01",
            customerId = customerUser.id,
            customerName = customerUser.fullName,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            totalAmount = 24900.0,
            status = OrderStatus.PAID.name,
            paystackRef = "PSTK_REF_01",
            deliveryCampus = "UNILAG",
            deliveryHostel = "Jaja Hall",
            contactPhone = "+2348123456789"
        )

        assertTrue(OrderSecurityPolicy.canReadOrder(order, customerUser))
        assertFalse(OrderSecurityPolicy.canReadOrder(order, otherCustomer))
        assertTrue(OrderSecurityPolicy.canReadOrder(order, sellerUser, sellerStoreId = "store_unilag_femzy"))
        assertFalse(OrderSecurityPolicy.canReadOrder(order, sellerUser, sellerStoreId = "other_store"))
        assertTrue(OrderSecurityPolicy.canReadOrder(order, adminUser))
    }

    @Test
    fun `RLS policy prevents customer from performing seller actions`() {
        val order = Order(
            id = "ord_unilag_02",
            customerId = customerUser.id,
            customerName = customerUser.fullName,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            totalAmount = 15000.0,
            status = OrderStatus.PAID.name,
            paystackRef = "PSTK_REF_02",
            deliveryCampus = "UNILAG",
            deliveryHostel = "Moremi Hall",
            contactPhone = "+2348123456789"
        )

        assertThrows(RlsPolicyViolationException::class.java) {
            OrderSecurityPolicy.validateUpdate(
                existingOrder = order,
                targetStatus = OrderStatus.PROCESSING,
                user = customerUser
            )
        }

        assertThrows(RlsPolicyViolationException::class.java) {
            OrderSecurityPolicy.validateDelete(order, customerUser)
        }
    }

    @Test
    fun `validation engine correctly validates product form inputs`() {
        val valid = ValidationEngine.validateProductForm(
            name = "Calculus Textbook",
            priceText = "4500",
            discountPriceText = "3500",
            quantityText = "2",
            category = "Books & Stationery",
            condition = "Like New",
            description = "Complete pages without marks",
            campus = "UNILAG"
        )
        assertTrue(valid.isValid)

        val invalidPrice = ValidationEngine.validateProductForm(
            name = "Calculus Textbook",
            priceText = "-100",
            category = "Books & Stationery",
            condition = "Like New",
            description = "Valid desc",
            campus = "UNILAG"
        )
        assertFalse(invalidPrice.isValid)

        val invalidDiscount = ValidationEngine.validateProductForm(
            name = "Calculus Textbook",
            priceText = "5000",
            discountPriceText = "6000",
            category = "Books & Stationery",
            condition = "Like New",
            description = "Valid desc",
            campus = "UNILAG"
        )
        assertFalse(invalidDiscount.isValid)
    }

    @Test
    fun `validation engine correctly validates profile form inputs`() {
        val valid = ValidationEngine.validateProfileForm(
            fullName = "Tunde Adeleke",
            email = "tunde@unilag.edu.ng",
            phone = "+2348123456789",
            matricNumber = "21/0458/ENG",
            campus = "UNILAG"
        )
        assertTrue(valid.isValid)

        val invalidEmail = ValidationEngine.validateProfileForm(
            fullName = "Tunde Adeleke",
            email = "not-an-email",
            phone = "08012345678",
            matricNumber = "21/0458/ENG",
            campus = "UNILAG"
        )
        assertFalse(invalidEmail.isValid)
        assertNotNull(invalidEmail.emailError)
    }

    @Test
    fun `order lifecycle execution via repository works end-to-end`() = runBlocking {
        val dummyProduct = Product(
            id = "prod_earbuds",
            sellerId = "seller_femzy",
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            name = "Test Wireless Earbuds",
            description = "Noise cancelling",
            price = 24000.0,
            category = "Phones & Accessories",
            condition = "Brand New"
        )

        val pendingResult = repository.createPendingOrder(
            customerId = customerUser.id,
            customerName = customerUser.fullName,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            items = listOf(dummyProduct to 1),
            deliveryCampus = "UNILAG",
            deliveryHostel = "Jaja Hall, Room B12",
            contactPhone = "+2348123456789"
        )
        assertTrue(pendingResult.isSuccess)
        val pendingOrder = pendingResult.getOrThrow()
        assertEquals(OrderStatus.PENDING.name, pendingOrder.status)

        val paidResult = repository.processOrderPayment(
            orderId = pendingOrder.id,
            paystackRef = "PSTK_VERIFIED_77819",
            actor = customerUser
        )
        assertTrue(paidResult.isSuccess)
        val paidOrder = paidResult.getOrThrow()
        assertEquals(OrderStatus.PAID.name, paidOrder.status)

        val dispatchResult = repository.updateOrderStatusSecure(
            orderId = paidOrder.id,
            targetStatus = OrderStatus.DISPATCHED,
            actor = sellerUser,
            sellerStoreId = "store_unilag_femzy"
        )
        assertTrue(dispatchResult.isSuccess)
        val dispatchedOrder = dispatchResult.getOrThrow()
        assertEquals(OrderStatus.DISPATCHED.name, dispatchedOrder.status)

        val deliveryResult = repository.deliverOrder(
            orderId = dispatchedOrder.id,
            actor = customerUser
        )
        assertTrue(deliveryResult.isSuccess)
        val deliveredOrder = deliveryResult.getOrThrow()
        assertEquals(OrderStatus.DELIVERED.name, deliveredOrder.status)

        val cancelAttempt = repository.cancelOrder(
            orderId = deliveredOrder.id,
            actor = customerUser,
            reason = "Want refund"
        )
        assertTrue(cancelAttempt.isFailure)
    }

    @Test
    fun `valid payment verification transitions order to PAID with paystack reference`() = runBlocking {
        val dummyProduct = Product(
            id = "prod_earbuds_02",
            sellerId = sellerUser.id,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            name = "Anker Earbuds",
            description = "Noise cancelling",
            price = 24000.0,
            category = "Phones & Accessories",
            condition = "Brand New"
        )

        val pendingResult = repository.createPendingOrder(
            customerId = customerUser.id,
            customerName = customerUser.fullName,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            items = listOf(dummyProduct to 1),
            deliveryCampus = "UNILAG",
            deliveryHostel = "Jaja Hall",
            contactPhone = "+2348123456789"
        )
        assertTrue(pendingResult.isSuccess)
        val pendingOrder = pendingResult.getOrThrow()
        assertEquals(OrderStatus.PENDING.name, pendingOrder.status)

        val verifyResult = repository.verifyAndFinalizeOrderPayment(
            orderId = pendingOrder.id,
            reference = "PSTK_TEST_VERIFIED_7721",
            expectedAmount = 24000.0,
            currency = "NGN"
        )
        assertTrue(verifyResult.isSuccess)
        val paidOrder = verifyResult.getOrThrow()
        assertEquals(OrderStatus.PAID.name, paidOrder.status)
        assertEquals("SUCCESSFUL", paidOrder.paymentStatus)
        assertEquals("PSTK_TEST_VERIFIED_7721", paidOrder.paystackRef)
    }

    @Test
    fun `failed transaction or invalid reference does not mark order as PAID`() = runBlocking {
        val dummyProduct = Product(
            id = "prod_laptop_stand",
            sellerId = sellerUser.id,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            name = "Aluminium Laptop Stand",
            description = "Adjustable height",
            price = 8500.0,
            category = "Gadgets",
            condition = "Brand New"
        )

        val pendingOrder = repository.createPendingOrder(
            customerId = customerUser.id,
            customerName = customerUser.fullName,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            items = listOf(dummyProduct to 1),
            deliveryCampus = "UNILAG",
            deliveryHostel = "New Hall",
            contactPhone = "+2348123456789"
        ).getOrThrow()

        val failResult = repository.verifyAndFinalizeOrderPayment(
            orderId = pendingOrder.id,
            reference = "PSTK_TEST_FAIL_001",
            expectedAmount = 8500.0,
            currency = "NGN"
        )
        assertTrue(failResult.isFailure)

        val orderInDb = database.marketplaceDao().getOrderDirect(pendingOrder.id)
        assertNotNull(orderInDb)
        assertEquals(OrderStatus.PENDING.name, orderInDb!!.status)
        assertEquals("FAILED", orderInDb.paymentStatus)
    }

    @Test
    fun `incorrect amount rejects verification and keeps order PENDING`() = runBlocking {
        val dummyProduct = Product(
            id = "prod_book_bio",
            sellerId = sellerUser.id,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            name = "Biology Textbook",
            description = "College biology",
            price = 5000.0,
            category = "Books & Stationery",
            condition = "Like New"
        )

        val pendingOrder = repository.createPendingOrder(
            customerId = customerUser.id,
            customerName = customerUser.fullName,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            items = listOf(dummyProduct to 1),
            deliveryCampus = "UNILAG",
            deliveryHostel = "Makama Hall",
            contactPhone = "+2348123456789"
        ).getOrThrow()

        val mismatchResult = repository.verifyAndFinalizeOrderPayment(
            orderId = pendingOrder.id,
            reference = "PSTK_TEST_MISMATCH_999",
            expectedAmount = 5000.0,
            currency = "NGN"
        )
        assertTrue(mismatchResult.isFailure)

        val orderInDb = database.marketplaceDao().getOrderDirect(pendingOrder.id)!!
        assertEquals(OrderStatus.PENDING.name, orderInDb.status)
        assertEquals("FAILED", orderInDb.paymentStatus)
    }

    @Test
    fun `incorrect currency rejects verification and keeps order PENDING`() = runBlocking {
        val dummyProduct = Product(
            id = "prod_shoes_01",
            sellerId = sellerUser.id,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            name = "Campus Sneakers",
            description = "White sneakers",
            price = 15000.0,
            category = "Shoes & Bags",
            condition = "Brand New"
        )

        val pendingOrder = repository.createPendingOrder(
            customerId = customerUser.id,
            customerName = customerUser.fullName,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            items = listOf(dummyProduct to 1),
            deliveryCampus = "UNILAG",
            deliveryHostel = "Mariere Hall",
            contactPhone = "+2348123456789"
        ).getOrThrow()

        val currencyResult = repository.verifyAndFinalizeOrderPayment(
            orderId = pendingOrder.id,
            reference = "PSTK_TEST_CURRENCY_77",
            expectedAmount = 15000.0,
            currency = "NGN"
        )
        assertTrue(currencyResult.isFailure)

        val orderInDb = database.marketplaceDao().getOrderDirect(pendingOrder.id)!!
        assertEquals(OrderStatus.PENDING.name, orderInDb.status)
        assertEquals("FAILED", orderInDb.paymentStatus)
    }

    @Test
    fun `duplicate verification is idempotent and does not corrupt order`() = runBlocking {
        val dummyProduct = Product(
            id = "prod_charger_fast",
            sellerId = sellerUser.id,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            name = "65W GaN Fast Charger",
            description = "Dual Type-C",
            price = 12000.0,
            category = "Gadgets",
            condition = "Brand New"
        )

        val pendingOrder = repository.createPendingOrder(
            customerId = customerUser.id,
            customerName = customerUser.fullName,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            items = listOf(dummyProduct to 1),
            deliveryCampus = "UNILAG",
            deliveryHostel = "Fagunwa Hall",
            contactPhone = "+2348123456789"
        ).getOrThrow()

        val firstResult = repository.verifyAndFinalizeOrderPayment(
            orderId = pendingOrder.id,
            reference = "PSTK_TEST_VERIFIED_DUPLICATE_1",
            expectedAmount = 12000.0,
            currency = "NGN"
        )
        assertTrue(firstResult.isSuccess)
        val paidOrder = firstResult.getOrThrow()
        assertEquals(OrderStatus.PAID.name, paidOrder.status)

        val secondResult = repository.verifyAndFinalizeOrderPayment(
            orderId = pendingOrder.id,
            reference = "PSTK_TEST_VERIFIED_DUPLICATE_1",
            expectedAmount = 12000.0,
            currency = "NGN"
        )
        assertTrue(secondResult.isSuccess)
        assertEquals(OrderStatus.PAID.name, secondResult.getOrThrow().status)
    }

    @Test
    fun `unauthorized customer cannot directly update order to PAID without server verification`() = runBlocking {
        val dummyProduct = Product(
            id = "prod_hoodie_01",
            sellerId = sellerUser.id,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            name = "Campus Hoodie",
            description = "Warm cotton hoodie",
            price = 11000.0,
            category = "Fashion",
            condition = "Brand New"
        )

        val pendingOrder = repository.createPendingOrder(
            customerId = customerUser.id,
            customerName = customerUser.fullName,
            storeId = "store_unilag_femzy",
            storeName = "Femzy Gadgets",
            items = listOf(dummyProduct to 1),
            deliveryCampus = "UNILAG",
            deliveryHostel = "Honours Hall",
            contactPhone = "+2348123456789"
        ).getOrThrow()

        val directUpdateResult = repository.updateOrderStatusSecure(
            orderId = pendingOrder.id,
            targetStatus = OrderStatus.PAID,
            actor = customerUser
        )
        assertTrue(directUpdateResult.isFailure)
        assertTrue(directUpdateResult.exceptionOrNull() is RlsPolicyViolationException)
        val errorMsg = directUpdateResult.exceptionOrNull()?.message ?: ""
        assertTrue(errorMsg.contains("Customers cannot directly mark an order as PAID"))
    }

    @Test
    fun `Product Room entity persists title price description and imageUrl`() = runBlocking {
        val newProduct = Product(
            id = "prod_student_listing_01",
            name = "Engineering Drawing Kit",
            title = "Engineering Drawing Kit",
            price = 6500.0,
            description = "Complete set of compasses, rulers, and T-square in great condition.",
            imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600"
        )
        database.marketplaceDao().insertProduct(newProduct)

        val fetched = database.marketplaceDao().getProductDirect("prod_student_listing_01")
        assertNotNull(fetched)
        assertEquals("Engineering Drawing Kit", fetched!!.title)
        assertEquals("Engineering Drawing Kit", fetched.name)
        assertEquals(6500.0, fetched.price, 0.01)
        assertEquals("Complete set of compasses, rulers, and T-square in great condition.", fetched.description)
        assertEquals("https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600", fetched.imageUrl)
    }
}


