package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.mobile.frotaviva_mobile.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_default)
        ViewCompat.getWindowInsetsController(window.decorView)
            ?.isAppearanceLightStatusBars = false

        binding.buttonContinueRegister.setOnClickListener {
            if (!validateInputs()) {
                Toast.makeText(this, "Preencha todos os campos para continuar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            navigateToRegisterPassword()
        }

        binding.buttonGoBack.setOnClickListener {
            val intent = Intent(this, Login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        return with(binding) {
            editTextNameRegister.text.isNotBlank() &&
                    editTextEmailRegister.text.isNotBlank() &&
                    editTextPhone.text.isNotBlank() &&
                    editTextCarPlate.text.isNotBlank() &&
                    editTextEnterpriseCode.text.isNotBlank()
        }
    }

    private fun navigateToRegisterPassword() {
        val intent = Intent(this, RegisterPassword::class.java)

        val bundle = Bundle().apply {
            putString("name", binding.editTextNameRegister.text.toString().trim())
            putString("email", binding.editTextEmailRegister.text.toString().trim())
            putString("phone", binding.editTextPhone.text.toString().trim())
            putString("car_plate", binding.editTextCarPlate.text.toString().trim())
            putString("enterprise_code", binding.editTextEnterpriseCode.text.toString().trim())
        }

        intent.putExtras(bundle)
        startActivity(intent)
    }
}