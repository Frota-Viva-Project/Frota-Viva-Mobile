package com.mobile.frotaviva_mobile.model

data class LocationUpdateRequest(
    val truckId: Int,
    val latitude: Double,
    val longitude: Double,
    val capturaLocalizacaoMl: Long = System.currentTimeMillis()
)