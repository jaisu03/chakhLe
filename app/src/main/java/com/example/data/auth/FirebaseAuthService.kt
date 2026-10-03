package com.example.data.auth

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

sealed class FirebaseAuthResult<out T> {
    data class Success<T>(val data: T) : FirebaseAuthResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : FirebaseAuthResult<Nothing>()
}

class FirebaseAuthService private constructor() {

    companion object {
        private const val TAG = "FirebaseAuthService"

        @Volatile
        private var instance: FirebaseAuthService? = null

        fun getInstance(): FirebaseAuthService {
            return instance ?: synchronized(this) {
                instance ?: FirebaseAuthService().also { instance = it }
            }
        }
    }

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAuth initialization notice: ${e.message}")
            null
        }
    }

    private var activeVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun isAvailable(): Boolean = firebaseAuth != null

    fun getCurrentUser(): FirebaseUser? = firebaseAuth?.currentUser

    fun getUserId(): String? = firebaseAuth?.currentUser?.uid

    fun getUserEmail(): String? = firebaseAuth?.currentUser?.email

    fun getActiveVerificationId(): String? = activeVerificationId

    /**
     * Dispatches real cellular SMS OTP to Indian phone (+91...) using Google Firebase Phone Auth.
     */
    fun startPhoneVerification(
        phone10: String,
        activity: Activity?,
        onCodeSent: (verificationId: String) -> Unit,
        onAutoVerified: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = firebaseAuth
        if (auth == null) {
            onError("Firebase Auth is not initialized. Please check network/services.")
            return
        }

        val fullPhoneNumber = "+91" + phone10.takeLast(10)
        Log.i(TAG, "Initiating Firebase Phone Auth for: $fullPhoneNumber")

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.i(TAG, "Firebase SMS OTP dispatched to $fullPhoneNumber. VerificationId: $verificationId")
                activeVerificationId = verificationId
                resendToken = token
                onCodeSent(verificationId)
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.i(TAG, "Firebase Phone verification completed automatically/instant.")
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null) {
                            onAutoVerified(user)
                        } else {
                            onError("Automatic sign-in succeeded but user profile was null")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Auto sign-in failure: ${e.message}")
                        onError(e.message ?: "Instant verification sign-in failed")
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Firebase Phone Auth failed for $fullPhoneNumber: ${e.message}", e)
                val friendlyMessage = when {
                    e.message?.contains("not configured for phone authentication", ignoreCase = true) == true ->
                        "Firebase Phone Auth requires enabling 'Phone' provider in Firebase Console (project chakhle-6df6b)."
                    e.message?.contains("app is not authorized", ignoreCase = true) == true ||
                    e.message?.contains("SHA-1", ignoreCase = true) == true ||
                    e.message?.contains("SHA-256", ignoreCase = true) == true ->
                        "App is not authorized in Firebase. Please add SHA-256 fingerprint in Firebase Console Project Settings."
                    e.message?.contains("BILLING", ignoreCase = true) == true ->
                        "Firebase daily SMS quota reached for today."
                    e.message?.contains("reCAPTCHA", ignoreCase = true) == true ->
                        "reCAPTCHA verification check failed on device: ${e.localizedMessage}"
                    e.message?.contains("Play Integrity", ignoreCase = true) == true ||
                    e.message?.contains("SafetyNet", ignoreCase = true) == true ->
                        "Play Integrity check requires SHA-256 registered in Firebase Console."
                    else -> e.localizedMessage ?: "Carrier SMS blocked or failed via Firebase"
                }
                onError(friendlyMessage)
            }
        }

        try {
            val builder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(fullPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbacks)

            if (activity != null) {
                builder.setActivity(activity)
            }

            if (resendToken != null) {
                builder.setForceResendingToken(resendToken!!)
            }

            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "PhoneAuthOptions build error: ${e.message}", e)
            onError(e.message ?: "Error launching Firebase Phone Verification")
        }
    }

    /**
     * Verifies the 6-digit SMS OTP code entered by the user against Firebase's verification session.
     */
    suspend fun verifyPhoneOtpCode(
        verificationId: String,
        smsCode: String
    ): FirebaseAuthResult<FirebaseUser> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext FirebaseAuthResult.Error("Firebase Auth unavailable")
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode.trim())
            suspendCancellableCoroutine<FirebaseAuthResult<FirebaseUser>> { continuation ->
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user != null) {
                            continuation.resume(FirebaseAuthResult.Success(user))
                        } else {
                            continuation.resume(FirebaseAuthResult.Error("Sign-in succeeded but user is null"))
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "verifyPhoneOtpCode failed: ${e.message}")
                        continuation.resume(FirebaseAuthResult.Error(e.message ?: "Invalid OTP code", e))
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "verifyPhoneOtpCode exception: ${e.message}")
            FirebaseAuthResult.Error(e.message ?: "Verification failed", e)
        }
    }

    /**
     * Sign Up with Email and Password using Firebase Auth
     */
    suspend fun signUpWithEmail(email: String, password: String): FirebaseAuthResult<FirebaseUser> =
        withContext(Dispatchers.IO) {
            val auth = firebaseAuth ?: return@withContext FirebaseAuthResult.Error(
                "Firebase Auth is not initialized. Please ensure google-services.json is configured."
            )

            suspendCancellableCoroutine<FirebaseAuthResult<FirebaseUser>> { continuation ->
                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user != null) {
                            Log.i(TAG, "Firebase user registered successfully: ${user.email} (UID: ${user.uid})")
                            continuation.resume(FirebaseAuthResult.Success(user))
                        } else {
                            continuation.resume(FirebaseAuthResult.Error("Sign-up succeeded but user object was null"))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Firebase sign-up error: ${exception.message}")
                        val friendlyMessage = when {
                            exception.message?.contains("email address is already in use", ignoreCase = true) == true ->
                                "An account with this email already exists. Please sign in instead."
                            exception.message?.contains("badly formatted", ignoreCase = true) == true ->
                                "Please enter a valid email address."
                            exception.message?.contains("at least 6 characters", ignoreCase = true) == true ->
                                "Password must be at least 6 characters long."
                            exception.message?.contains("network error", ignoreCase = true) == true ->
                                "Network connection error. Please check your internet connection."
                            else -> exception.localizedMessage ?: "Firebase Sign-Up failed"
                        }
                        continuation.resume(FirebaseAuthResult.Error(friendlyMessage, exception))
                    }
            }
        }

    /**
     * Sign In with Email and Password using Firebase Auth
     */
    suspend fun signInWithEmail(email: String, password: String): FirebaseAuthResult<FirebaseUser> =
        withContext(Dispatchers.IO) {
            val auth = firebaseAuth ?: return@withContext FirebaseAuthResult.Error(
                "Firebase Auth is not initialized. Please ensure google-services.json is configured."
            )

            suspendCancellableCoroutine<FirebaseAuthResult<FirebaseUser>> { continuation ->
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user != null) {
                            Log.i(TAG, "Firebase user signed in: ${user.email} (UID: ${user.uid})")
                            continuation.resume(FirebaseAuthResult.Success(user))
                        } else {
                            continuation.resume(FirebaseAuthResult.Error("Sign-in succeeded but user object was null"))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Firebase sign-in error: ${exception.message}")
                        val friendlyMessage = when {
                            exception.message?.contains("no user record", ignoreCase = true) == true ||
                            exception.message?.contains("user-not-found", ignoreCase = true) == true ->
                                "No user found with this email. Please sign up first."
                            exception.message?.contains("wrong-password", ignoreCase = true) == true ||
                            exception.message?.contains("invalid-credential", ignoreCase = true) == true ->
                                "Incorrect password. Please verify your credentials or reset password."
                            exception.message?.contains("too-many-requests", ignoreCase = true) == true ->
                                "Access temporarily disabled due to many failed attempts. Try again later."
                            else -> exception.localizedMessage ?: "Firebase Sign-In failed"
                        }
                        continuation.resume(FirebaseAuthResult.Error(friendlyMessage, exception))
                    }
            }
        }

    /**
     * Send Password Reset Email via Firebase Auth
     */
    suspend fun sendPasswordReset(email: String): FirebaseAuthResult<Unit> =
        withContext(Dispatchers.IO) {
            val auth = firebaseAuth ?: return@withContext FirebaseAuthResult.Error("Firebase Auth is not available")

            suspendCancellableCoroutine<FirebaseAuthResult<Unit>> { continuation ->
                auth.sendPasswordResetEmail(email.trim())
                    .addOnSuccessListener {
                        Log.i(TAG, "Password reset email sent to: $email")
                        continuation.resume(FirebaseAuthResult.Success(Unit))
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Password reset error: ${exception.message}")
                        continuation.resume(
                            FirebaseAuthResult.Error(
                                exception.localizedMessage ?: "Failed to send password reset email",
                                exception
                            )
                        )
                    }
            }
        }

    /**
     * Signs in or creates a customer profile after successful Email OTP verification.
     * Generates or retrieves Firebase user session for the verified email.
     */
    suspend fun ensureSessionForVerifiedEmail(email: String): FirebaseUser? =
        withContext(Dispatchers.IO) {
            val auth = firebaseAuth ?: return@withContext null
            val cleanEmail = email.trim().lowercase()

            // If already signed in with matching email, return current user
            val current = auth.currentUser
            if (current != null && current.email?.equals(cleanEmail, ignoreCase = true) == true) {
                return@withContext current
            }

            // Create or sign in user with a stable, secure hash derived password for OTP sessions
            val otpPass = "ChakhLe!Otp$" + cleanEmail.hashCode()
            return@withContext try {
                val signInResult = suspendCancellableCoroutine<FirebaseUser?> { cont ->
                    auth.signInWithEmailAndPassword(cleanEmail, otpPass)
                        .addOnSuccessListener { cont.resume(it.user) }
                        .addOnFailureListener { cont.resume(null) }
                }
                if (signInResult != null) {
                    signInResult
                } else {
                    // Try creating the account
                    suspendCancellableCoroutine<FirebaseUser?> { cont ->
                        auth.createUserWithEmailAndPassword(cleanEmail, otpPass)
                            .addOnSuccessListener { cont.resume(it.user) }
                            .addOnFailureListener {
                                Log.w(TAG, "Could not auto-create Firebase user: ${it.message}")
                                cont.resume(null)
                            }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "ensureSessionForVerifiedEmail error: ${e.message}")
                null
            }
        }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
            Log.i(TAG, "Firebase user signed out")
        } catch (e: Exception) {
            Log.w(TAG, "Error during signOut: ${e.message}")
        }
    }
}
