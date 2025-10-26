package com.mobile.frotaviva_mobile.auth

data class LoginResponse(
    val token: String,
    val expiresIn: Int,
    val tokenType: String
)