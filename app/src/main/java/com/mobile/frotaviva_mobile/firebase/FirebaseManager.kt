package com.mobile.frotaviva_mobile.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest


class FirebaseManager {
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun registerUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    task.exception?.let { onFailure(it) }
                }
            }
    }

    fun updateUserProfile(displayName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser
        user?.let {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()

            it.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        task.exception?.let { exception -> onFailure(exception) }
                    }
                }
        } ?: onFailure(Exception("Usuário não está logado"))
    }

    fun loginUser(email: String, password: String, onSuccess: (FirebaseUser) -> Unit, onFailure: (Exception) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(task.result.user!!)
                } else {
                    onFailure(task.exception!!)
                }
            }
    }

    fun logoutUser() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}