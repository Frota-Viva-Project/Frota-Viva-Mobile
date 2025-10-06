package com.mobile.frotaviva_mobile.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Maintenance(
    val titulo: String,
    @SerializedName("tipoManutencao")
    val tipo: String,
    val info: String,
    val dataOcorrido: Date,
    val tempoManutencao: Int,
    val servico: List<Service>
)