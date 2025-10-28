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

        // Back button functionality
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Edit Profile icon click
        findViewById<CardView>(R.id.editIconContainer).setOnClickListener {
            Toast.makeText(this, "Funcionalidade de editar perfil ainda não implementada.", Toast.LENGTH_SHORT).show()
        }

        // Menu items click listeners
        val menuCard = findViewById<CardView>(R.id.menuCard)
        // Note: This relies heavily on the internal structure (LinearLayout) and index of the children in activity_profile.xml
        val linearLayout = menuCard.getChildAt(0) as? LinearLayout

        linearLayout?.let {
            // Item 0: Termos de Uso
            it.getChildAt(0).setOnClickListener {
                // 🚀 CORREÇÃO AQUI: Sintaxe Kotlin para Intent, apontando para UserTermsActivity 🚀
                val intent = Intent(this, UserTermsActivity::class.java)
                startActivity(intent)
            }

            // Item 2: Cartões e Pagamento
            it.getChildAt(2).setOnClickListener {
                Toast.makeText(this, "Navegar para Cartões e Pagamento", Toast.LENGTH_SHORT).show()
            }

            // Item 4: Ajuda
            it.getChildAt(4).setOnClickListener {
                Toast.makeText(this, "Navegar para Ajuda", Toast.LENGTH_SHORT).show()
            }

            // Item 6: Edição de Senha
            it.getChildAt(6).setOnClickListener {
                Toast.makeText(this, "Navegar para tela de Edição de Senha", Toast.LENGTH_SHORT).show()
            }

            // Item 8: Verificar informações
            it.getChildAt(8).setOnClickListener {
                Toast.makeText(this, "Navegar para Verificar informações", Toast.LENGTH_SHORT).show()
            }
        }
    }
}