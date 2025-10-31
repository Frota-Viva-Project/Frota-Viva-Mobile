package com.mobile.frotaviva_mobile.model

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("id")
    val id: Int,

    @SerializedName("userId")
    val userId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("body")
    val body: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("errorMessage")
    val errorMessage: String?,

    @SerializedName("sentAt")
    val sentAt: String,

    val isRead: Boolean = false
)
