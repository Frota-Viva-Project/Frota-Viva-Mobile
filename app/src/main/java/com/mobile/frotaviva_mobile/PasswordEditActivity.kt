package com.mobile.frotaviva_mobile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
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
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_edit)

        firebaseAuth = FirebaseAuth.getInstance()

        oldPasswordInput = findViewById(R.id.editTextOldPassword)
        newPasswordInput = findViewById(R.id.editTextNewPassword)
        modifyButton = findViewById(R.id.buttonModifyPassword)
        backButton = findViewById(R.id.backButton)
        progressBar = findViewById(R.id.progressBar)

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

    private fun showError(textView: TextView, message: String) {
        textView.text = message
        textView.visibility = View.VISIBLE
    }

    private fun clearErrors() {
        textErrorOld.visibility = View.GONE
        textErrorNew.visibility = View.GONE
        textErrorOld.text = ""
        textErrorNew.text = ""
    }

    private fun showLoading(isLoading: Boolean) {
        modifyButton.isEnabled = !isLoading
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun handleChangePassword() {
        clearErrors()

        val oldPassword = oldPasswordInput.text.toString().trim()
        val newPassword = newPasswordInput.text.toString().trim()

        var hasError = false
        if (oldPassword.isEmpty()) {
            showError(textErrorOld, "Por favor, informe sua senha antiga.")
            hasError = true
        }
        if (newPassword.isEmpty()) {
            showError(textErrorNew, "Por favor, informe a nova senha.")
            hasError = true
        }
        if (hasError) return

        if (newPassword.length < 6) {
            showError(textErrorNew, "A nova senha deve ter pelo menos 6 caracteres.")
            return
        }

        val user = firebaseAuth.currentUser
        if (user == null || user.email == null) {
            showError(textErrorOld, "Usuário não encontrado. Faça login novamente.")
            return
        }

        showLoading(true)

        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                Log.d("PasswordEdit", "Reautenticação bem-sucedida.")
                updatePassword(newPassword)
            }
            .addOnFailureListener { e ->
                Log.w("PasswordEdit", "Reautenticação falhou", e)
                showError(textErrorOld, "Senha antiga incorreta.")
                showLoading(false)
            }
    }

    private fun updatePassword(newPassword: String) {
        val user = firebaseAuth.currentUser

        user?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                Log.d("PasswordEdit", "Senha atualizada com sucesso.")
                Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show()
                showLoading(false)
                finish()
            }
            ?.addOnFailureListener { e ->
                Log.w("PasswordEdit", "Falha ao atualizar senha", e)
                showError(textErrorNew, "Erro ao atualizar senha: ${e.message}")
                showLoading(false)
            }
    }
}