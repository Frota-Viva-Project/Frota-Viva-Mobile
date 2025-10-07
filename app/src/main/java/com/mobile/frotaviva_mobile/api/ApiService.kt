package com.mobile.frotaviva_mobile.api

import com.mobile.frotaviva_mobile.model.Alert
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("alerta/caminhao/{id}")
    suspend fun getAlerts(
        @Path("id") id: Int
    ): Response<List<Alert>>
}