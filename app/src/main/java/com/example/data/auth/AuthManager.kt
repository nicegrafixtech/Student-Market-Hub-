package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthManager(private val context: Context) {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        try {
            _currentUser.value = firebaseAuth.currentUser
            firebaseAuth.addAuthStateListener { auth ->
                _currentUser.value = auth.currentUser
            }
        } catch (e: Exception) {
            Log.w("AuthManager", "Firebase initialization check: ${e.message}")
        }
    }

    fun isUserSignedIn(): Boolean = firebaseAuth.currentUser != null

    fun getFirebaseUser(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun registerWithEmail(
        email: String,
        pass: String,
        fullName: String,
        campus: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: return@withContext Result.failure(Exception("Registration failed: no user returned"))

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(fullName.trim())
                .build()
            user.updateProfile(profileUpdates).await()

            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthManager", "Error registering student with email", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(
        email: String,
        pass: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: return@withContext Result.failure(Exception("Sign in failed: no user returned"))
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthManager", "Error signing in student with email", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(webClientId: String? = null): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val effectiveClientId = webClientId ?: "default-student-market-hub-client"
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(effectiveClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user ?: return@withContext Result.failure(Exception("Google Sign-In returned null user"))
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Google Sign-In cancelled"))
        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "Credential Manager error", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthManager", "Google sign-in error", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            firebaseAuth.signOut()
            _currentUser.value = null
        } catch (e: Exception) {
            Log.w("AuthManager", "Sign out error", e)
        }
    }
}
