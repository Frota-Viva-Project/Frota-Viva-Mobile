package com.mobile.frotaviva_mobile.model

data class TruckResponse(
    val id: Int,
    val placa: String,
    val modelo: String,
    val status: String,
    val capacidade: Int,
    val motorista: DriverInfo
)

data class DriverInfo(
    val id: Int
)