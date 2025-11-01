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
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.mobile.frotaviva_mobile.auth.JwtUtils
import com.mobile.frotaviva_mobile.storage.SecureStorage

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var secureStorage: SecureStorage
    private lateinit var nameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        secureStorage = SecureStorage(this)
        nameTextView = findViewById(R.id.nameTextView)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!isUserAuthenticated()) {
            redirectToLogin()
            return
        }

        loadUserName()

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
                startActivity(Intent(this, UserTermsActivity::class.java))
            }
            it.getChildAt(2).setOnClickListener {
                startActivity(Intent(this, PaymentActivity::class.java))
            }
            it.getChildAt(4).setOnClickListener {
                startActivity(Intent(this, HelpActivity::class.java))
            }
            it.getChildAt(6).setOnClickListener {
                startActivity(Intent(this, PasswordEditActivity::class.java))
            }
            it.getChildAt(8).setOnClickListener {
                startActivity(Intent(this, VerifyInfoActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check authentication again when resuming
        if (!isUserAuthenticated()) {
            redirectToLogin()
            return
        }
        loadUserName()
    }

    private fun isUserAuthenticated(): Boolean {
        val token = secureStorage.getToken()

        if (!token.isNullOrEmpty() && !JwtUtils.isTokenExpired(token)) {
            return true
        }

        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            return false
        }

        return false
    }

    private fun loadUserName() {
        val user = auth.currentUser
        if (user != null) {
            val userName = user.displayName
            nameTextView.text = if (!userName.isNullOrBlank()) userName else "Usuário Sem Nome"
        } else {
            val token = secureStorage.getToken()
            if (!token.isNullOrEmpty()) {
                try {
                    val name = JwtUtils.getNameFromToken(token)
                    nameTextView.text = if (!name.isNullOrBlank()) name else "Usuário Sem Nome"
                } catch (e: Exception) {
                    nameTextView.text = "Usuário Sem Nome"
                }
            } else {
                nameTextView.text = "Usuário Sem Nome"
            }
        }
    }

    private fun redirectToLogin() {
        Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show()

        secureStorage.clearToken()
        auth.signOut()

        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}