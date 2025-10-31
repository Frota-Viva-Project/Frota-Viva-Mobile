package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.frotaviva_mobile.storage.SecureStorage

class VerifyInfoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var secureStorage: SecureStorage
    private var currentUserId: String? = null

    // UI Elements
    private lateinit var textNome: TextView
    private lateinit var textEmail: TextView
    private lateinit var textPlaca: TextView
    private lateinit var textTelefone: TextView

    private var currentDialog: AlertDialog? = null
    private val TAG = "VerifyInfoActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_info)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        secureStorage = SecureStorage(this)

        textNome = findViewById(R.id.textNome)
        textEmail = findViewById(R.id.textEmail)
        textPlaca = findViewById(R.id.textPlaca)
        textTelefone = findViewById(R.id.textTelefone)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        loadUserData()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.buttonLogout).setOnClickListener { logout() }

        findViewById<ImageButton>(R.id.editNome).setOnClickListener {
            val currentValue = textNome.text.toString().substringAfter(": ").trim()
            showEditFieldDialog("Nome", currentValue, "Insira o seu novo nome", "Seu nome")
        }
        findViewById<ImageButton>(R.id.editPlaca).setOnClickListener {
            val currentValue = textPlaca.text.toString().substringAfter(": ").trim()
            showEditFieldDialog("Placa", currentValue, "Insira a sua nova placa", "ABC1D23")
        }
        findViewById<ImageButton>(R.id.editTelefone).setOnClickListener {
            val currentValue = textTelefone.text.toString().substringAfter(": ").trim()
            showEditFieldDialog("Telefone", currentValue, "Insira o seu novo telefone", "(11) 9....-....")
        }
        findViewById<ImageButton>(R.id.editEmail).setOnClickListener {
            val currentValue = textEmail.text.toString().substringAfter(": ").trim()
            showEditEmailDialog(currentValue)
        }
        findViewById<ImageButton>(R.id.editSenha).setOnClickListener {
            showEditPasswordDialog()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser

        currentUserId = user?.uid

        textNome.text = "Nome: ${user?.displayName ?: "Não definido"}"
        textEmail.text = "Email: ${user?.email ?: "Não definido"}"

        currentUserId?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        textPlaca.text = "Placa: ${document.getString("placa") ?: "Não definida"}"
                        textTelefone.text = "Telefone: ${document.getString("telefone") ?: "Não definido"}"
                    } else {
                        textPlaca.text = "Placa: Não definida"
                        textTelefone.text = "Telefone: Não definido"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching user data", e)
                    Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            textPlaca.text = "Placa: Não definida"
            textTelefone.text = "Telefone: Não definido"
        }
    }

    private fun logout() {
        auth.signOut()
        secureStorage.clearToken()
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showDialogError(errorView: TextView, message: String) {
        errorView.text = message
        try {
            val errorColor = ContextCompat.getColor(this, R.color.feedback_error_default)
            errorView.setTextColor(errorColor)
        } catch (e: Exception) {
            errorView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }
        errorView.visibility = View.VISIBLE
    }

    private fun showEditFieldDialog(fieldName: String, currentValue: String, subtitle: String, hint: String) {
        // Este dialog é para campos que NÃO precisam de reautenticação (Nome, Placa, Telefone)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_field, null)

        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val sub = dialogView.findViewById<TextView>(R.id.dialogSubtitle)
        val editText = dialogView.findViewById<EditText>(R.id.editTextValue)
        val errorText = dialogView.findViewById<TextView>(R.id.dialogError)
        val btnCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)

        title.text = "Editar $fieldName"
        sub.text = subtitle
        editText.setText(currentValue)
        editText.hint = hint

        when (fieldName) {
            "Telefone" -> editText.inputType = android.text.InputType.TYPE_CLASS_PHONE
            else -> editText.inputType = android.text.InputType.TYPE_CLASS_TEXT
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        currentDialog = dialog

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val newValue = editText.text.toString().trim()
            if (newValue.isEmpty()) {
                showDialogError(errorText, "Campo não pode ser vazio")
                return@setOnClickListener
            }

            errorText.visibility = View.GONE
            btnConfirm.isEnabled = false
            btnCancel.isEnabled = false

            when (fieldName) {
                "Nome" -> updateDisplayName(newValue)
                "Placa" -> updateFirestoreField("placa", newValue)
                "Telefone" -> updateFirestoreField("telefone", newValue)
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showEditEmailDialog(currentEmail: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_email, null)

        val editNewEmail = dialogView.findViewById<EditText>(R.id.editTextNewEmail)
        val editPassword = dialogView.findViewById<EditText>(R.id.editTextPassword)
        val errorEmail = dialogView.findViewById<TextView>(R.id.dialogErrorEmail)
        val errorPassword = dialogView.findViewById<TextView>(R.id.dialogErrorPassword)
        val btnCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)

        editNewEmail.setText(currentEmail)
        editNewEmail.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        currentDialog = dialog

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val newEmail = editNewEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            errorEmail.visibility = View.GONE
            errorPassword.visibility = View.GONE

            var hasError = false
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                showDialogError(errorEmail, "Formato de email inválido.")
                hasError = true
            }
            if (password.isEmpty()) {
                showDialogError(errorPassword, "Informe sua senha para confirmar.")
                hasError = true
            }
            if (hasError) return@setOnClickListener

            btnConfirm.isEnabled = false
            btnCancel.isEnabled = false

            reauthenticateAndUpdateEmail(newEmail, password, errorPassword, btnConfirm, btnCancel, dialog)
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun reauthenticateAndUpdateEmail(
        newEmail: String,
        password: String,
        errorView: TextView,
        btnConfirm: Button,
        btnCancel: Button,
        dialog: AlertDialog
    ) {
        val user = auth.currentUser

        if (user?.email == null) {
            Toast.makeText(this, "Erro: Usuário não encontrado ou sem e-mail.", Toast.LENGTH_SHORT).show()
            btnConfirm.isEnabled = true
            btnCancel.isEnabled = true
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                Log.d(TAG, "Reautenticação de e-mail bem-sucedida. Tentando atualizar...")
                user.updateEmail(newEmail)
                    .addOnSuccessListener {
                        Log.d(TAG, "Email atualizado no Auth com sucesso.")
                        updateFirestoreField("email", newEmail)
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Falha ao atualizar email", e)
                        showDialogError(errorView, "Erro ao atualizar email: ${e.message}")
                        btnConfirm.isEnabled = true
                        btnCancel.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Reautenticação para e-mail falhou", e)
                showDialogError(errorView, "Senha incorreta. Reautenticação falhou.")
                btnConfirm.isEnabled = true
                btnCancel.isEnabled = true
            }
    }


    private fun showEditPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_password, null)

        val editOldPass = dialogView.findViewById<EditText>(R.id.editTextOldPassword)
        val editNewPass = dialogView.findViewById<EditText>(R.id.editTextNewPassword)
        val errorOld = dialogView.findViewById<TextView>(R.id.dialogErrorOld)
        val errorNew = dialogView.findViewById<TextView>(R.id.dialogErrorNew)
        val btnCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        currentDialog = dialog

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val oldPassword = editOldPass.text.toString().trim()
            val newPassword = editNewPass.text.toString().trim()

            errorOld.visibility = View.GONE
            errorNew.visibility = View.GONE

            var hasError = false
            if (oldPassword.isEmpty()) {
                showDialogError(errorOld, "Por favor, informe sua senha antiga.")
                hasError = true
            }
            if (newPassword.isEmpty()) {
                showDialogError(errorNew, "Por favor, informe a nova senha.")
                hasError = true
            }
            if (hasError) return@setOnClickListener

            if (newPassword.length < 6) {
                showDialogError(errorNew, "A nova senha deve ter pelo menos 6 caracteres.")
                return@setOnClickListener
            }

            btnConfirm.isEnabled = false
            btnCancel.isEnabled = false

            val user = auth.currentUser

            if (user?.email == null) {
                Toast.makeText(this, "Erro: Usuário não encontrado ou sem e-mail.", Toast.LENGTH_SHORT).show()
                btnConfirm.isEnabled = true
                btnCancel.isEnabled = true
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    Log.d(TAG, "Reautenticação bem-sucedida.")
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            Log.d(TAG, "Senha atualizada com sucesso.")
                            Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Falha ao atualizar senha", e)
                            showDialogError(errorNew, "Erro ao atualizar senha: ${e.message}")
                            btnConfirm.isEnabled = true
                            btnCancel.isEnabled = true
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Reautenticação falhou", e)
                    showDialogError(errorOld, "Senha antiga incorreta.")
                    btnConfirm.isEnabled = true
                    btnCancel.isEnabled = true
                }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun updateDisplayName(newName: String) {
        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateFirestoreField("nome", newName)
            } else {
                Toast.makeText(this, "Falha ao atualizar nome.", Toast.LENGTH_SHORT).show()
                currentDialog?.dismiss()
            }
        }
    }

    private fun updateFirestoreField(field: String, value: String) {
        // ... (função já estava correta)
        if (currentUserId == null) {
            Toast.makeText(this, "Erro: ID de usuário não encontrado.", Toast.LENGTH_SHORT).show()
            currentDialog?.dismiss()
            return
        }

        db.collection("users").document(currentUserId!!)
            .update(field, value)
            .addOnSuccessListener {
                Toast.makeText(this, "${field.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                loadUserData()
                currentDialog?.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao atualizar $field.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error updating $field", e)
                currentDialog?.dismiss()
            }
    }
}