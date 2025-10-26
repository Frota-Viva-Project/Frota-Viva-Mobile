package com.mobile.frotaviva_mobile.auth

import com.mobile.frotaviva_mobile.storage.SecureStorage
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log

class AuthInterceptor(private val secureStorage: SecureStorage) : Interceptor {
    private val AUTH_HEADER = "Authorization"
    private val TOKEN_TYPE = "Bearer"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = secureStorage.getToken()

        if (token.isNullOrEmpty()) {
            Log.w("AuthInterceptor", "Nenhum token encontrado. Requisição enviada sem autenticação.")
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header(AUTH_HEADER, "$TOKEN_TYPE $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}