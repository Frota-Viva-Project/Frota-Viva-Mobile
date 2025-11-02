package com.mobile.frotaviva_mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.frotaviva_mobile.auth.JwtUtils
import com.mobile.frotaviva_mobile.storage.SecureStorage
import com.mobile.frotaviva_mobile.util.CloudinaryManager
import java.io.File
import java.io.FileOutputStream

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var secureStorage: SecureStorage

    private lateinit var nameTextView: TextView
    private lateinit var profileImageView: ShapeableImageView

    private var cameraImageUri: Uri? = null
    private var currentDialog: AlertDialog? = null
    private val TAG = "ProfileActivity"

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(this, "Permissão de câmera negada.", Toast.LENGTH_SHORT).show()
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraImageUri?.let { uri ->
                    currentDialog?.dismiss()
                    uploadImageToCloudinary(uri)
                }
            } else {
                Toast.makeText(this, "Falha ao capturar imagem.", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                currentDialog?.dismiss()
                uploadImageToCloudinary(uri)
            } else {
                Toast.makeText(this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        secureStorage = SecureStorage(this)

        nameTextView = findViewById(R.id.nameTextView)
        profileImageView = findViewById(R.id.profileImageView)

        CloudinaryManager.init(applicationContext)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!isUserAuthenticated()) {
            redirectToLogin()
            return
        }

        loadUserProfile()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<CardView>(R.id.editIconContainer).setOnClickListener {
            showEditPhotoDialog()
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
        if (!isUserAuthenticated()) {
            redirectToLogin()
            return
        }
        loadUserProfile()
    }

    private fun isUserAuthenticated(): Boolean {
        val token = secureStorage.getToken()
        if (!token.isNullOrEmpty() && !JwtUtils.isTokenExpired(token)) {
            return true
        }
        val firebaseUser = auth.currentUser
        return firebaseUser != null
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val userName = user.displayName
            nameTextView.text = if (!userName.isNullOrBlank()) userName else "Usuário Sem Nome"

            db.collection("driver").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val photoUrl = document.getString("photoUrl")
                        if (!photoUrl.isNullOrEmpty()) {
                            loadProfilePhoto(photoUrl)
                        } else {
                            profileImageView.setImageResource(R.drawable.profile_picture)
                        }
                    } else {
                        profileImageView.setImageResource(R.drawable.profile_picture)
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Falha ao buscar photoUrl", it)
                    profileImageView.setImageResource(R.drawable.profile_picture)
                }

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

    private fun loadProfilePhoto(url: String) {
        Glide.with(this)
            .load(url)
            .circleCrop() // Aplica o corte circular
            .placeholder(R.drawable.profile_picture)
            .error(R.drawable.profile_picture)
            .into(profileImageView)
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

    private fun showEditPhotoDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_photo_options, null)

        val btnTakePhoto = dialogView.findViewById<Button>(R.id.buttonTakePhoto)
        val btnChooseGallery = dialogView.findViewById<Button>(R.id.buttonChooseGallery)
        val btnCancel = dialogView.findViewById<Button>(R.id.buttonCancel)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        currentDialog = dialog

        btnTakePhoto.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        btnChooseGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "Precisamos da câmera para tirar a foto.", Toast.LENGTH_SHORT).show()
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        val photoFile = File(applicationContext.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")

        val localImageUri: Uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            photoFile
        )

        cameraImageUri = localImageUri

        cameraLauncher.launch(localImageUri)
    }

    private fun getPathFromUri(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp_gallery_image.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao copiar URI para arquivo", e)
            null
        }
    }

    private fun uploadImageToCloudinary(uri: Uri) {
        val filePath = getPathFromUri(uri)

        if (filePath == null) {
            Toast.makeText(this, "Falha ao processar a imagem selecionada.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Iniciando upload...", Toast.LENGTH_SHORT).show()

        CloudinaryManager.uploadImage(
            filePath,
            onSuccess = { secureUrl ->
                updateProfilePhotoInFirestore(secureUrl)
            },
            onError = { errorMsg ->
                Toast.makeText(this, "Erro no Upload: $errorMsg", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Erro Cloudinary: $errorMsg")
            }
        )
    }

    private fun updateProfilePhotoInFirestore(photoUrl: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Erro: Usuário não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("driver").document(user.uid)
            .update("photoUrl", photoUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show()
                loadProfilePhoto(photoUrl)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Falha ao salvar a foto no perfil.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Erro ao atualizar Firestore", e)
            }
    }
}