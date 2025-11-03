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
    private const val BASE_CHAT_URL = "https://chatbot-api-xung.onrender.com"

    @Volatile
    private lateinit var apiServiceInstance: ApiService

    @Volatile
    private lateinit var chatBotServiceInstance: ApiService

    private lateinit var sharedOkHttpClient: OkHttpClient

    fun initialize(context: Context) {
        if (::apiServiceInstance.isInitialized && ::chatBotServiceInstance.isInitialized) return

        val secureStorage = SecureStorage(context.applicationContext)
        val authInterceptor = AuthInterceptor(secureStorage)

        sharedOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(500, TimeUnit.SECONDS)
            .readTimeout(500, TimeUnit.SECONDS)
            .writeTimeout(500, TimeUnit.SECONDS)
            .build()

        val retrofitMain = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(sharedOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val retrofitChatbot = Retrofit.Builder()
            .baseUrl(BASE_CHAT_URL)
            .client(sharedOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiServiceInstance = retrofitMain.create(ApiService::class.java)
        chatBotServiceInstance = retrofitChatbot.create(ApiService::class.java)
    }

    val instance: ApiService
        get() {
            check(::apiServiceInstance.isInitialized) {
                "RetrofitClient não foi inicializado. Chame RetrofitClient.initialize(context) primeiro."
            }
            return apiServiceInstance
        }

    val chatbotInstance: ApiService
        get() {
            check(::chatBotServiceInstance.isInitialized) {
                "RetrofitClient não foi inicializado. Chame RetrofitClient.initialize(context) primeiro."
            }
            return chatBotServiceInstance
        }
}