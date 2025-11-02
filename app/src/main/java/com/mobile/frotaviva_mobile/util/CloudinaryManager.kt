package com.mobile.frotaviva_mobile.util // Crie este pacote se não existir

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.File

object CloudinaryManager {

    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to "dzqorhtj8",
            "api_key" to "363595826919881",
            "api_secret" to "OQ7kaKqiumEHYfzm-6dohDzSkCQ"
        )
        MediaManager.init(context, config)
    }

    fun uploadImage(
        filePath: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        MediaManager.get().upload(filePath)
            .unsigned("frotaviva_preset")
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        onSuccess(secureUrl)
                    } else {
                        onError("Falha ao obter URL do Cloudinary.")
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    onError("Erro no upload: ${error.description}")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    onError("Upload reagendado: ${error.description}")
                }
            })
            .dispatch()
    }
}