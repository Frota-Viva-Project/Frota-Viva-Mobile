package com.mobile.frotaviva_mobile.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseManager {
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        enterpriseCode: String,
        truckId: Int,
        carModel: String,
        carPlate: String,
        photoUrl: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        val driverData = hashMapOf(
                            "email" to email,
                            "name" to name,
                            "phone" to phone,
                            "enterpriseCode" to enterpriseCode,
                            "truckId" to truckId,
                            "carModel" to carModel,
                            "carPlate" to carPlate,
                            "photoUrl" to photoUrl,
                            "userId" to null,
                            "backendTruckId" to null
                        )

                        db.collection("driver").document(user.uid)
                            .set(driverData)
                            .addOnSuccessListener {
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()

                                user.updateProfile(profileUpdates)
                                    .addOnSuccessListener {
                                        onSuccess(user)
                                    }
                                    .addOnFailureListener { e ->
                                        onFailure(e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                onFailure(e)
                            }
                    } else {
                        onFailure(Exception("Erro ao criar usuário Firebase"))
                    }
                } else {
                    task.exception?.let { onFailure(it) }
                }
            }
    }

    fun updateUserIds(uid: String, userId: Int, truckId: Int) {
        val updates = mapOf(
            "userId" to userId,
            "backendTruckId" to truckId
        )

        db.collection("driver").document(uid)
            .update(updates)
            .addOnSuccessListener {
                println("IDs atualizados com sucesso no Firestore.")
            }
            .addOnFailureListener { e ->
                println("Falha ao atualizar IDs no Firestore: ${e.localizedMessage}")
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(task.result.user!!)
                } else {
                    onFailure(task.exception ?: Exception("Erro desconhecido no login"))
                }
            }
    }
}