package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobile.frotaviva_mobile.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val intent = Intent(this,RegisterPassword::class.java);
        val bundle = Bundle()

        binding.buttonNavigateLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        binding.buttonContinue.setOnClickListener {
            bundle.putString("name", binding.editTextName.text.toString())
            bundle.putString("email", binding.editTextEmail.text.toString())
            bundle.putString("phone", binding.editTextPhone.text.toString())
            bundle.putString("car_plate", binding.editTextCarPlate.text.toString())
            bundle.putString("enterprise_code", binding.editTextEnterprisrCode.text.toString())

            intent.putExtras(bundle)

            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}