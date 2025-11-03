package com.mobile.frotaviva_mobile.model

data class Notification(
    val id: Int = 0,
    val userId: Int = 0,
    val status: String = "NEW",
    val errorMessage: String? = null,
    val sentAt: String? = null,
    val title: String? = null,
    val body: String? = null
)
