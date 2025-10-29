package com.mobile.frotaviva_mobile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class PasswordEditActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var oldPasswordInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var modifyButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var textErrorOld: TextView
    private lateinit var textErrorNew: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_edit)

        firebaseAuth = FirebaseAuth.getInstance()

        oldPasswordInput = findViewById(R.id.editTextOldPassword)
        newPasswordInput = findViewById(R.id.editTextNewPassword)
        modifyButton = findViewById(R.id.buttonModifyPassword)
        backButton = findViewById(R.id.backButton)

        textErrorOld = findViewById(R.id.textErrorOldPassword)
        textErrorNew = findViewById(R.id.textErrorNewPassword)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        modifyButton.setOnClickListener {
            handleChangePassword()
        }
    }

    private fun clearErrors() {
        textErrorOld.visibility = View.GONE
        textErrorNew.visibility = View.GONE
        textErrorOld.text = ""
        textErrorNew.text = ""
    }

    private fun handleChangePassword() {
        clearErrors()

        val oldPassword = oldPasswordInput.text.toString().trim()
        val newPassword = newPasswordInput.text.toString().trim()

        var hasError = false
        if (oldPassword.isEmpty()) {
            textErrorOld.text = "Por favor, informe sua senha antiga."
            textErrorOld.visibility = View.VISIBLE
            hasError = true
        }
        if (newPassword.isEmpty()) {
            textErrorNew.text = "Por favor, informe a nova senha."
            textErrorNew.visibility = View.VISIBLE
            hasError = true
        }
        if (hasError) return

        if (newPassword.length < 6) {
            textErrorNew.text = "A nova senha deve ter pelo menos 6 caracteres."
            textErrorNew.visibility = View.VISIBLE
            return
        }

        val user = firebaseAuth.currentUser
        if (user == null || user.email == null) {
            textErrorOld.text = "Usuário não encontrado. Faça login novamente."
            textErrorOld.visibility = View.VISIBLE
            return
        }

        modifyButton.isEnabled = false

        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                Log.d("PasswordEdit", "Reautenticação bem-sucedida.")
                updatePassword(newPassword)
            }
            .addOnFailureListener { e ->
                Log.w("PasswordEdit", "Reautenticação falhou", e)
                textErrorOld.text = "Senha antiga incorreta."
                textErrorOld.visibility = View.VISIBLE
                modifyButton.isEnabled = true
            }
    }

    private fun updatePassword(newPassword: String) {
        val user = firebaseAuth.currentUser

        user?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                Log.d("PasswordEdit", "Senha atualizada com sucesso.")
                Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show()
                finish()
            }
            ?.addOnFailureListener { e ->
                Log.w("PasswordEdit", "Falha ao atualizar senha", e)
                textErrorNew.text = "Erro ao atualizar senha: ${e.message}"
                textErrorNew.visibility = View.VISIBLE
                modifyButton.isEnabled = true
            }
    }
}