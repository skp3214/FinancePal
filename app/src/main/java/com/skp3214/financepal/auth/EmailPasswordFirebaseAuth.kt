package com.skp3214.financepal.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EmailPasswordFirebaseAuth {

    private val tag = "AuthRepository: "

    private val firebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    suspend fun register(email: String, password: String): RegistrationResult {
        return try {
            val existingUser = suspendCoroutine { continuation ->
                firebaseAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val signInMethods = task.result?.signInMethods ?: emptyList<String>()
                            continuation.resume(signInMethods.isNotEmpty())
                        } else {
                            continuation.resume(false)
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(false)
                    }
            }

            if (existingUser) {
                Log.d(tag, "User already exists with this email: $email")
                return RegistrationResult.UserExists
            }

            val result = suspendCoroutine { continuation ->
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        continuation.resume(task.isSuccessful)
                    }
                    .addOnFailureListener {
                        continuation.resume(false)
                    }
            }
            if (result) RegistrationResult.Success else RegistrationResult.Failure
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            RegistrationResult.Failure
        }
    }

    suspend fun login(email: String, password: String): LoginResult {
        return try {
            val result = suspendCoroutine { continuation ->
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(LoginResult.Success)
                        } else {
                            continuation.resume(LoginResult.Failure)
                        }
                    }
                    .addOnFailureListener { _ ->
                        continuation.resume(LoginResult.Failure)
                    }
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) {
                throw e
            }
            LoginResult.Failure
        }
    }
}

sealed class RegistrationResult {
    data object Success : RegistrationResult()
    data object UserExists : RegistrationResult()
    data object Failure : RegistrationResult()
}

sealed class LoginResult {
    data object Success : LoginResult()
    data object Failure : LoginResult()
}