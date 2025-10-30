package com.mobile.frotaviva_mobile.auth

import com.mobile.frotaviva_mobile.storage.SecureStorage
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log

class AuthInterceptor(private val secureStorage: SecureStorage) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = secureStorage.getToken()

        val requestWithToken = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(requestWithToken)

        if (response.code == 401) {
            Log.w("AuthInterceptor", "Token expirado ou inválido (401). Limpando armazenamento.")
            secureStorage.clearToken()
        }

        return response
    }
}