package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.cardview.widget.CardView
import android.widget.LinearLayout
import android.widget.Toast

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<CardView>(R.id.editIconContainer).setOnClickListener {
            Toast.makeText(this, "Funcionalidade de editar perfil ainda não implementada.", Toast.LENGTH_SHORT).show()
        }

        val menuCard = findViewById<CardView>(R.id.menuCard)
        val linearLayout = menuCard.getChildAt(0) as? LinearLayout

        linearLayout?.let {
            it.getChildAt(0).setOnClickListener {
                val intent = Intent(this, UserTermsActivity::class.java)
                startActivity(intent)
            }

            it.getChildAt(2).setOnClickListener {
                val intent = Intent(this, PaymentActivity::class.java)
                startActivity(intent)
            }

            it.getChildAt(4).setOnClickListener {
                val intent = Intent(this, HelpActivity::class.java)
                startActivity(intent)
            }

            it.getChildAt(6).setOnClickListener {
                val intent = Intent(this, PasswordEditActivity::class.java)
                startActivity(intent)
            }

            it.getChildAt(8).setOnClickListener {
                val intent = Intent(this, VerifyInfoActivity::class.java)
                startActivity(intent)
            }
        }
    }
}