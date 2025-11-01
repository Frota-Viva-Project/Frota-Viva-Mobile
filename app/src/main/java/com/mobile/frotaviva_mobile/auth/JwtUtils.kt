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

    fun getNameFromToken(token: String): String? {
        if (token.isEmpty()) return null

        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE)
            val payload = String(decodedBytes, Charsets.UTF_8)
            val json = JSONObject(payload)

            when {
                json.has("name") -> json.getString("name")
                json.has("displayName") -> json.getString("displayName")
                json.has("username") -> json.getString("username")
                json.has("email") -> json.getString("email")
                else -> null
            }
        } catch (e: Exception) {
            Log.e("JWT_ERROR", "Erro ao extrair nome do token: ", e)
            null
        }
    }

    fun getEmailFromToken(token: String): String? {
        if (token.isEmpty()) return null

        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE)
            val payload = String(decodedBytes, Charsets.UTF_8)
            val json = JSONObject(payload)

            if (json.has("email")) {
                json.getString("email")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("JWT_ERROR", "Erro ao extrair email do token: ", e)
            null
        }
    }

    fun getUserIdFromToken(token: String): String? {
        if (token.isEmpty()) return null

        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE)
            val payload = String(decodedBytes, Charsets.UTF_8)
            val json = JSONObject(payload)

            when {
                json.has("sub") -> json.getString("sub")
                json.has("userId") -> json.getString("userId")
                json.has("uid") -> json.getString("uid")
                json.has("id") -> json.getString("id")
                else -> null
            }
        } catch (e: Exception) {
            Log.e("JWT_ERROR", "Erro ao extrair ID do token: ", e)
            null
        }
    }

    fun getAllClaims(token: String): Map<String, Any>? {
        if (token.isEmpty()) return null

        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE)
            val payload = String(decodedBytes, Charsets.UTF_8)
            val json = JSONObject(payload)

            val claims = mutableMapOf<String, Any>()
            json.keys().forEach { key ->
                claims[key] = json.get(key)
            }
            claims
        } catch (e: Exception) {
            Log.e("JWT_ERROR", "Erro ao extrair claims do token: ", e)
            null
        }
    }
}