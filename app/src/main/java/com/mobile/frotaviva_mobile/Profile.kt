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
                Toast.makeText(this, "Navegar para Cartões e Pagamento", Toast.LENGTH_SHORT).show()
            }

            it.getChildAt(4).setOnClickListener {
                Toast.makeText(this, "Navegar para Ajuda", Toast.LENGTH_SHORT).show()
            }

            it.getChildAt(6).setOnClickListener {
                Toast.makeText(this, "Navegar para tela de Edição de Senha", Toast.LENGTH_SHORT).show()
            }

            it.getChildAt(8).setOnClickListener {
                Toast.makeText(this, "Navegar para Verificar informações", Toast.LENGTH_SHORT).show()
            }
        }
    }
}