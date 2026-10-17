package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    /**
     * Silent auto sign-in using GetGoogleIdOption in its own dedicated GetCredentialRequest.
     */
    fun attemptAutoSignIn(
        activityContext: Context,
        scope: CoroutineScope,
        onAuthSuccess: (FirebaseUser) -> Unit = {},
        onUnauthenticated: () -> Unit = {}
    ) {
        val existingUser = try { firebaseAuth.currentUser } catch (e: Exception) { null }
        if (existingUser != null) {
            _currentUser.value = existingUser
            onAuthSuccess(existingUser)
            return
        }

        val clientId = try {
            activityContext.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activityContext, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        _currentUser.value = user
                        onAuthSuccess(user)
                    } else {
                        onUnauthenticated()
                    }
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    /**
     * Interactive Google Sign-In using GetSignInWithGoogleOption in its own dedicated GetCredentialRequest.
     */
    suspend fun signInWithGoogle(
        activityContext: Context = context,
        onAuthCancelled: () -> Unit = {}
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val clientId = try {
            activityContext.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return@withContext Result.failure(
                Exception("Google Sign-In configuration missing: default_web_client_id not found")
            )
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        try {
            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = activityContext as? Activity ?: activityContext
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                    ?: return@withContext Result.failure(Exception("Google Sign-In returned null user"))
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            withContext(Dispatchers.Main) { onAuthCancelled() }
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e("Auth", "Google Sign-In credential error", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    fun signOut(scope: CoroutineScope? = null, onSignOutComplete: () -> Unit = {}) {
        try {
            firebaseAuth.signOut()
            _currentUser.value = null
            if (scope != null) {
                scope.launch {
                    try {
                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    } catch (e: Exception) {
                        Log.e("Auth", "Failed to clear credential state", e)
                    } finally {
                        onSignOutComplete()
                    }
                }
            } else {
                onSignOutComplete()
            }
        } catch (e: Exception) {
            Log.w("AuthManager", "Sign out error", e)
            onSignOutComplete()
        }
    }
}
