package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        binding = ActivityRegisterPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseManager = FirebaseManager()

            val receivedBundle = intent.extras
        val name = receivedBundle?.getString("name") ?: ""
        val email = receivedBundle?.getString("email") ?: ""
        val phone = receivedBundle?.getString("phone") ?: ""
        val carPlate = receivedBundle?.getString("car_plate") ?: ""
        val enterpriseCode = receivedBundle?.getString("enterprise_code") ?: ""

        binding.buttonGoBack.setOnClickListener {
            finish()
        }

        binding.buttonContinueRegister.setOnClickListener {
            val password = binding.editTextPasswordRegister.text.toString()
            val confirmPassword = binding.editTextConfirmPasswordRegister.text.toString()

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Preencha a senha e a confirmação.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(name, email, phone, carPlate, enterpriseCode, password)
        }
    }

    private fun registerUser(name: String, email: String, phone: String, carPlate: String, enterpriseCode: String, password: String) {
        firebaseManager.registerUser(email, password,
            onSuccess = {
                updateProfileAndSaveData(name, email, phone, carPlate, enterpriseCode)
            },
            onFailure = {
                Toast.makeText(this, "Falha no cadastro: Usuário já existe ou senha fraca.", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun updateProfileAndSaveData(name: String, email: String, phone: String, carPlate: String, enterpriseCode: String) {
        firebaseManager.updateUserProfile(name,
            onSuccess = {
                saveUserData(name, email, phone, carPlate, enterpriseCode)
            },
            onFailure = {
                Toast.makeText(this, "Erro ao salvar nome de perfil.", Toast.LENGTH_SHORT).show()
                saveUserData(name, email, phone, carPlate, enterpriseCode)
            }
        )
    }

    private fun saveUserData(name: String, email: String, phone: String, carPlate: String, enterpriseCode: String) {
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
                    Toast.makeText(this, "Cadastro concluído com sucesso!", Toast.LENGTH_LONG).show()
                    // 3. Redireciona para a tela principal
                    redirectToMain()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao salvar dados do motorista.", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "Erro: UID do usuário ausente.", Toast.LENGTH_LONG).show()
        }
    }
    private fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}