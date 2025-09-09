package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.mobile.frotaviva_mobile.databinding.ActivityLoginBinding
import com.mobile.frotaviva_mobile.firebase.FirebaseManager

class Login : AppCompatActivity() {
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        firebaseManager = FirebaseManager()

        if(firebaseManager.isUserLoggedIn()) {

        }

        binding.navigateToRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        binding.navigateToRegister2.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }


        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            loginUser(email, password)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loginUser(email: String, password: String) {
        firebaseManager.loginUser(email, password, onSuccess = {
            redirectToMain()
        }, onFailure = { exception ->
            handleLoginError(exception)
        })
    }

    private fun redirectToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun handleLoginError(exception: Exception?) {
        val message = when (exception) {
            is FirebaseAuthInvalidUserException -> "Invalid user"
            is FirebaseAuthInvalidCredentialsException -> "Invalid credentials"
            else -> exception?.localizedMessage ?: "Unknown error"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}