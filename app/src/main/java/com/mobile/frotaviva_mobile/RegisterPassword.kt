package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.ActivityRegisterPasswordBinding
import com.mobile.frotaviva_mobile.firebase.FirebaseManager
import com.mobile.frotaviva_mobile.model.DriverRequest
import com.mobile.frotaviva_mobile.model.TruckResponse
import com.mobile.frotaviva_mobile.storage.SecureStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

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
        val carModel = receivedBundle?.getString("car_model") ?: ""
        val capacity = receivedBundle?.getInt("capacity") ?: 0

        binding.buttonGoBack.setOnClickListener { finish() }

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

            registerUser(name, email, phone, carPlate, enterpriseCode, password, carModel, capacity)
        }
    }

    private fun registerUser(
        name: String,
        email: String,
        phone: String,
        carPlate: String,
        enterpriseCode: String,
        password: String,
        carModel: String,
        capacity: Int
    ) {
        firebaseManager.registerUser(email, password,
            onSuccess = {
                updateProfileAndSaveData(name, email, phone, carPlate, enterpriseCode, carModel, capacity)
            },
            onFailure = {
                Toast.makeText(this, "Falha no cadastro: Usuário já existe ou senha fraca.", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun updateProfileAndSaveData(
        name: String,
        email: String,
        phone: String,
        carPlate: String,
        enterpriseCode: String,
        carModel: String,
        capacity: Int
    ) {
        firebaseManager.updateUserProfile(name,
            onSuccess = {
                saveUserData(name, email, phone, carPlate, enterpriseCode, carModel, capacity)
            },
            onFailure = {
                Toast.makeText(this, "Erro ao salvar nome de perfil.", Toast.LENGTH_SHORT).show()
                saveUserData(name, email, phone, carPlate, enterpriseCode, carModel, capacity)
            }
        )
    }

    private fun saveUserData(
        name: String,
        email: String,
        phone: String,
        carPlate: String,
        enterpriseCode: String,
        carModel: String,
        capacity: Int
    ) {
        val uid = Firebase.auth.currentUser?.uid

        if (uid != null) {
            val user = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "carPlate" to carPlate,
                "enterpriseCode" to enterpriseCode,
                "carModel" to carModel
            )

            Firebase.firestore.collection("driver").document(uid)
                .set(user)
                .addOnSuccessListener {
                    // Após salvar no Firestore, chamar API para vincular motorista
                    fetchTruckData(carPlate, carModel, capacity, enterpriseCode)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao salvar dados do motorista.", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "Erro: UID do usuário ausente.", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchTruckData(carPlate: String, carModel: String, capacity: Int, enterpriseCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = DriverRequest(
                    placa = carPlate,
                    modelo = carModel,
                    capacidade = capacity
                )

                val response: Response<TruckResponse> =
                    RetrofitClient.instance.linkDriver(enterpriseCode, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val truckId = body?.id ?: 0
                    val userId = body?.motorista?.id ?: 0

                    val uid = Firebase.auth.currentUser?.uid
                    if (uid != null) {
                        // Salva truckId e userId no Firestore
                        Firebase.firestore.collection("driver").document(uid)
                            .update("truckId", truckId, "userId", userId)
                            .addOnSuccessListener {
                                // Salva localmente no SecureStorage
                                val storage = SecureStorage(this@RegisterPassword)
                                storage.saveTruckId(truckId)
                                storage.saveUserId(userId)

                                runOnUiThread { redirectToMain() }
                            }
                            .addOnFailureListener {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@RegisterPassword,
                                        "Erro ao salvar truckId/userId no Firestore.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RegisterPassword,
                            "Erro ao vincular motorista: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterPassword,
                        "Falha na requisição: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
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
