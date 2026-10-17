package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firestore.FirestoreProductRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createProductListing_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FirestoreProductRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createProductListing(
                title = "Engineering Mathematics Textbook",
                price = 4500.0,
                description = "Clean pages, no marks",
                imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c"
            )
        }
        assertTrue(createResult.isSuccess)
        val productId = createResult.getOrThrow()
        assertTrue(productId.isNotEmpty())
    }

    @Test
    fun getUserProducts_authenticatedOwner_returnsMatchingProducts() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FirestoreProductRepository(firestore)
        val productId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createProductListing(
                title = "USB-C Laptop Hub",
                price = 9500.0,
                description = "7-in-1 multiport adapter",
                imageUrl = "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46"
            ).getOrThrow()
        }

        val productsResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getUserProducts() }
        assertTrue(productsResult.isSuccess)
        assertTrue(productsResult.getOrThrow().map { it.id }.contains(productId))
    }

    @Test
    fun observeUserProducts_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FirestoreProductRepository(firestore)
        val productId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createProductListing(
                title = "Rechargeable Study Lamp",
                price = 6000.0,
                description = "Lasts 8 hours during hostel power cuts",
                imageUrl = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c"
            ).getOrThrow()
        }

        val emittedProducts = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProducts().first { list -> list.any { it.id == productId } }
        }
        assertTrue(emittedProducts.map { it.id }.contains(productId))
    }

    @Test
    fun getProductById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreProductRepository(firestore)
        val productId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createProductListing(
                title = "Alice Private Calculator",
                price = 12000.0,
                description = "Casio fx-991EX",
                imageUrl = "https://example.com/calc.jpg"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = FirestoreProductRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getProductById(productId) }
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull(exception)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    @Test
    fun observeUserProducts_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = FirestoreProductRepository(firestore)

        var caughtException: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeUserProducts().first()
            }
        } catch (e: FirebaseFirestoreException) {
            caughtException = e
        }
        assertNotNull(caughtException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, caughtException?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
