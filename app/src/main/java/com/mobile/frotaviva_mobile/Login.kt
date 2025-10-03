package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.mobile.frotaviva_mobile.databinding.ActivityLoginBinding
import com.mobile.frotaviva_mobile.firebase.FirebaseManager

class Login : AppCompatActivity() {
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseManager = FirebaseManager()
        FirebaseApp.initializeApp(this)

        if (firebaseManager.isUserLoggedIn()) {
            redirectToMain()
            return
        }

        binding.navigateToRegister.setOnClickListener {
            // Navega para a tela de cadastro
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

        FirebaseApp.initializeApp(this)
    }

    private fun loginUser(email: String, password: String) {
        firebaseManager.loginUser(email, password,
            onSuccess = {
                redirectToMain()
            },
            onFailure = { exception ->
                handleLoginError(exception)
            }
        )
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
}