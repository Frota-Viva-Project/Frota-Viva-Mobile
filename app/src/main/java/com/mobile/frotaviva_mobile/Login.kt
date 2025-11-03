package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.auth.JwtUtils
import com.mobile.frotaviva_mobile.auth.TokenExchangeRequest
import com.mobile.frotaviva_mobile.databinding.ActivityLoginBinding
import com.mobile.frotaviva_mobile.firebase.FirebaseManager
import com.mobile.frotaviva_mobile.storage.SecureStorage
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var binding: ActivityLoginBinding
    private lateinit var secureStorage: SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_default)
        ViewCompat.getWindowInsetsController(window.decorView)
            ?.isAppearanceLightStatusBars = false

        firebaseManager = FirebaseManager()
        FirebaseApp.initializeApp(this)
        secureStorage = SecureStorage(this)

        RetrofitClient.initialize(applicationContext)
        checkPersistentLogin()

        binding.navigateToRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmailLogin.text.toString().trim()
            val password = binding.editTextPasswordLogin.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        firebaseManager.loginUser(email, password,
            onSuccess = { firebaseUser ->
                getFirebaseTokenAndExchange(firebaseUser)
            },
            onFailure = { exception ->
                handleLoginError(exception)
            }
        )
    }

    private fun getFirebaseTokenAndExchange(firebaseUser: FirebaseUser) {
        firebaseUser.getIdToken(true)
            .addOnSuccessListener { result ->
                val firebaseIdToken = result.token
                if (firebaseIdToken != null) {
                    exchangeTokenWithBackend(firebaseIdToken, firebaseUser.uid)
                } else {
                    handleLoginError(null)
                }
            }
            .addOnFailureListener { e ->
                handleLoginError(e)
            }
    }

    private fun exchangeTokenWithBackend(firebaseIdToken: String, uid: String) {
        lifecycleScope.launch {
            try {
                val authService = RetrofitClient.instance
                val request = TokenExchangeRequest(firebaseIdToken = firebaseIdToken)
                val response = authService.exchangeFirebaseToken(request)

                if (response.isSuccessful && response.body() != null) {
                    val yourJwt = response.body()!!.token
                    secureStorage.saveToken(yourJwt)

                    Firebase.firestore.collection("driver").document(uid)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val truckId = (document.getLong("backendTruckId") ?: 0).toInt()
                                val userId = (document.getLong("userId") ?: 0).toInt()

                                if (truckId > 0 && userId > 0) {
                                    secureStorage.saveTruckId(truckId)
                                    secureStorage.saveUserId(userId)
                                    redirectToMain()
                                } else {
                                    Toast.makeText(this@Login, "Truck ID ou User ID não encontrado.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@Login, "Documento do usuário não encontrado.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@Login, "Erro ao buscar dados do usuário.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@Login, "Falha de autorização no servidor.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Login, "Erro de conexão com o servidor.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun handleLoginError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> "Usuário não encontrado. Verifique seu e-mail."
            is FirebaseAuthInvalidCredentialsException -> "Senha incorreta."
            else -> exception?.localizedMessage ?: "Erro desconhecido. Tente novamente."
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun checkPersistentLogin() {
        val token = secureStorage.getToken()

        if (!token.isNullOrEmpty() && !JwtUtils.isTokenExpired(token)) {
            redirectToMain()
        } else {
            secureStorage.clearToken()
        }
    }
}