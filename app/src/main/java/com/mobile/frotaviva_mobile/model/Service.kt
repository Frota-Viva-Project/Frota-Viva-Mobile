package com.mobile.frotaviva_mobile.model

import com.google.gson.annotations.SerializedName

data class Service(
    val id: Int,
    @SerializedName("descServico")
    val descricaoServico: String,
    val custo: Double,
    val dataInicio: String,
    val dataConclusao: String
)