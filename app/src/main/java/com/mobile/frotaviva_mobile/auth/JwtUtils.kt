package com.mobile.frotaviva_mobile.auth

import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object JwtUtils {
    fun isTokenExpired(token: String): Boolean {
        if (token.isEmpty()) return true

        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE)
            val payload = String(decodedBytes, Charsets.UTF_8)

            val json = JSONObject(payload)
            val expTimestamp = json.getLong("exp")

            val currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

            currentTime >= expTimestamp

        } catch (e: Exception) {
            Log.e("JWT_ERROR", "Houve um erro: ", e)
            false
        }
    }
}