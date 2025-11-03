package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseUser
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.auth.TokenExchangeRequest
import com.mobile.frotaviva_mobile.databinding.ActivityRegisterPasswordBinding
import com.mobile.frotaviva_mobile.firebase.FirebaseManager
import com.mobile.frotaviva_mobile.model.DriverRequest
import com.mobile.frotaviva_mobile.storage.SecureStorage
import kotlinx.coroutines.launch

class RegisterPassword : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterPasswordBinding
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var secureStorage: SecureStorage

    private var email: String? = null
    private var name: String? = null
    private var phone: String? = null
    private var enterpriseCode: String? = null
    private var carModel: String? = null
    private var carPlate: String? = null
    private var capacity: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseManager = FirebaseManager()
        secureStorage = SecureStorage(this)

        email = intent.getStringExtra("email")
        name = intent.getStringExtra("name")
        phone = intent.getStringExtra("phone")
        enterpriseCode = intent.getStringExtra("enterprise_code")
        carModel = intent.getStringExtra("car_model")
        carPlate = intent.getStringExtra("car_plate")
        capacity = intent.getIntExtra("capacity", 0)

        binding.buttonContinueRegister.setOnClickListener {
            val password = binding.editTextPasswordRegister.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPasswordRegister.text.toString().trim()

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(password)
        }
    }

    private fun registerUser(password: String) {
        val userEmail = email ?: return
        val userName = name ?: return
        val userPhone = phone ?: return
        val userEnterpriseCode = enterpriseCode ?: return

        firebaseManager.registerUser(
            email = userEmail,
            password = password,
            name = userName,
            phone = userPhone,
            enterpriseCode = userEnterpriseCode,
            truckId = 0,
            carModel = carModel ?: "",
            carPlate = carPlate ?: "",
            photoUrl = "",
            onSuccess = { firebaseUser ->
                Toast.makeText(this, "Usuário criado no Firebase!", Toast.LENGTH_SHORT).show()
                getFirebaseTokenAndExchange(firebaseUser)
            },
            onFailure = { exception ->
                Toast.makeText(this, "Erro ao registrar: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun getFirebaseTokenAndExchange(firebaseUser: FirebaseUser) {
        firebaseUser.getIdToken(true)
            .addOnSuccessListener { result ->
                val firebaseIdToken = result.token
                if (firebaseIdToken != null) {
                    exchangeTokenWithBackend(firebaseUser, firebaseIdToken)
                } else {
                    Toast.makeText(this, "Erro ao gerar token do Firebase.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Falha ao pegar token: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun exchangeTokenWithBackend(firebaseUser: FirebaseUser, firebaseIdToken: String) {
        lifecycleScope.launch {
            try {
                val authService = RetrofitClient.instance
                val request = TokenExchangeRequest(firebaseIdToken = firebaseIdToken)

                val response = authService.exchangeFirebaseToken(request)
                if (response.isSuccessful && response.body() != null) {
                    val jwtToken = response.body()!!.token
                    secureStorage.saveToken(jwtToken)

                    val driverRequest = DriverRequest(
                        placa = carPlate ?: "",
                        modelo = carModel ?: "",
                        capacidade = capacity ?: 0
                    )

                    val motoristaResponse = authService.linkDriver(
                        codEmpresa = enterpriseCode ?: "",
                        request = driverRequest
                    )

                    if (motoristaResponse.isSuccessful && motoristaResponse.body() != null) {
                        val motorista = motoristaResponse.body()!!
                        val userId = motorista.motorista.id
                        val truckId = motorista.id

                        secureStorage.saveUserId(userId)
                        secureStorage.saveTruckId(truckId)

                        firebaseManager.updateUserIds(firebaseUser.uid, userId, truckId)

                        Toast.makeText(this@RegisterPassword, "Registro concluído!", Toast.LENGTH_SHORT).show()
                        redirectToMain()
                    } else {
                        Toast.makeText(
                            this@RegisterPassword,
                            "Erro ao vincular motorista (${motoristaResponse.code()}).",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this@RegisterPassword, "Erro ao trocar token com o servidor.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterPassword, "Erro de conexão: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}