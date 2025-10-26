package com.mobile.frotaviva_mobile.api

import android.content.Context
import com.mobile.frotaviva_mobile.auth.AuthInterceptor
import com.mobile.frotaviva_mobile.storage.SecureStorage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api-postgresql-kr87.onrender.com/v1/api/"

    @Volatile
    private lateinit var apiServiceInstance: ApiService

    fun initialize(context: Context) {
        if (::apiServiceInstance.isInitialized) {
            return
        }

        val secureStorage = SecureStorage(context.applicationContext)

        val authInterceptor = AuthInterceptor(secureStorage)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiServiceInstance = retrofit.create(ApiService::class.java)
    }

    val instance: ApiService
        get() {
            check(::apiServiceInstance.isInitialized) {
                "RetrofitClient não foi inicializado. Chame RetrofitClient.initialize(context) primeiro."
            }
            return apiServiceInstance
        }
}