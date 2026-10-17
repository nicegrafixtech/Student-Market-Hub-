package com.example.data.firestore

import android.content.Context
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

@IgnoreExtraProperties
data class FirestoreProductListing(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "Phones & Accessories",
    val campus: String = "University of Lagos (UNILAG)",
    val status: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class FirestoreProductRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun createProductListing(
        title: String,
        price: Double,
        description: String,
        imageUrl: String,
        category: String = "Phones & Accessories",
        campus: String = "University of Lagos (UNILAG)"
    ): Result<String> {
        val uid = requireUserId()
        val productId = "prod_" + UUID.randomUUID().toString().replace("-", "").take(12)
        val docRef = db.collection("products").document(productId)
        val payload = mapOf(
            "userId" to uid,
            "title" to title,
            "price" to price,
            "description" to description,
            "imageUrl" to imageUrl,
            "category" to category,
            "campus" to campus,
            "status" to "ACTIVE",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(productId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getUserProducts(): Result<List<FirestoreProductListing>> {
        val uid = requireUserId()
        val path = "products"
        return try {
            val snapshot = db.collection(path)
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val list = snapshot.toObjects(
                FirestoreProductListing::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun getProductById(productId: String): Result<FirestoreProductListing?> {
        val docRef = db.collection("products").document(productId)
        return try {
            val snapshot = docRef.get().await()
            val item = snapshot.toObject(
                FirestoreProductListing::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(item)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    fun observeUserProducts(): Flow<List<FirestoreProductListing>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "products"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FirestoreProductListing::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun updateProductListing(listing: FirestoreProductListing): Result<Unit> {
        requireUserId()
        require(listing.id.isNotBlank()) { "Listing ID must not be blank for update" }
        requireNotNull(listing.createdAt) { "Listing createdAt must not be null for update" }
        val docRef = db.collection("products").document(listing.id)
        return try {
            docRef.set(listing.copy(updatedAt = null)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteProductListing(productId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("products").document(productId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
