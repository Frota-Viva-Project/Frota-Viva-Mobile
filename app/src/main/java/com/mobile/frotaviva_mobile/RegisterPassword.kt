package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.mobile.frotaviva_mobile.databinding.ActivityRegisterPasswordBinding
import com.mobile.frotaviva_mobile.firebase.FirebaseManager

class RegisterPassword : AppCompatActivity() {
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var binding: ActivityRegisterPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val receivedBundle = intent.extras
        val name = receivedBundle?.getString("name") ?: ""
        val email = receivedBundle?.getString("email") ?: ""
        val phone = receivedBundle?.getString("phone") ?: ""
        val carPlate = receivedBundle?.getString("car_plate") ?: ""
        val enterpriseCode = receivedBundle?.getString("enterprise_code") ?: ""

        binding.buttonGoBack.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        binding.buttonContinueRegister.setOnClickListener {
            val password = binding.editTextNameRegister.text.toString()
            if (password == binding.editTextEmailRegister.text.toString()) {
                registerUser(name, email, phone, carPlate, enterpriseCode, password)
            }
        }
    }

    private fun registerUser(name: String, email: String, phone: String, carPlate: String, enterpriseCode: String, password: String) {
        firebaseManager.registerUser(email, password, onSuccess = {
            Toast.makeText(this, "Success register user", Toast.LENGTH_SHORT).show()
            updateProfile(name)
            val uid = Firebase.auth.currentUser?.uid
            if (uid != null) {
                val user = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "carPlate" to carPlate,
                    "enterpriseCode" to enterpriseCode
                )
                Firebase.firestore.collection("driver").document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        Toast.makeText(this, "User data saved", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                    }
            }
        }, onFailure = {
            Toast.makeText(this, "Failed register user", Toast.LENGTH_SHORT).show()
        })
    }

    private fun updateProfile(name: String) {
        firebaseManager.updateUserProfile(name, onSuccess = {
            finish()
        }, onFailure = {
            Toast.makeText(this, "Failed update user profile", Toast.LENGTH_SHORT).show()
        })
    }
}
