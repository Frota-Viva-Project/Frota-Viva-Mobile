package com.mobile.frotaviva_mobile.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import android.util.Log

class SecureStorage(private val context: Context) {
    private val PREF_KEY_JWT = "app_jwt_token"
    private val PREF_FILE_NAME = "secure_prefs"

    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            EncryptedSharedPreferences.create(
                PREF_FILE_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("SecureStorage", "Falha ao inicializar o EncryptedSharedPreferences: ${e.message}")
            context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE)
        }
    }

    fun saveToken(token: String) {
        encryptedPrefs.edit()
            .putString(PREF_KEY_JWT, token)
            .apply()
        Log.d("SecureStorage", "JWT salvo com sucesso (criptografado).")
    }

    fun getToken(): String? {
        return encryptedPrefs.getString(PREF_KEY_JWT, null)
    }

    fun clearToken() {
        encryptedPrefs.edit()
            .remove(PREF_KEY_JWT)
            .apply()
        Log.d("SecureStorage", "JWT removido.")
    }
}