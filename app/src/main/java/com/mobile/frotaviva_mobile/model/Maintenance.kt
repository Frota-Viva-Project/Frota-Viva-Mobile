package com.mobile.frotaviva_mobile.model

import java.util.Date

data class Maintenance(
    val titulo: String,
    val info: String,
    val dataOcorrido: Date,
    val status: String,
)