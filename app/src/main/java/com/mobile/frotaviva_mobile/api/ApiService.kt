package com.mobile.frotaviva_mobile.api

import com.mobile.frotaviva_mobile.model.Maintenance
import com.mobile.frotaviva_mobile.model.Alert
import com.mobile.frotaviva_mobile.model.Route
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("manutencao/caminhao/{id}")
    suspend fun getMaintenances(
        @Path("id") id: Int
    ): Response<List<Maintenance>>
  
    @GET("alerta/{id}")
    suspend fun getAlerts(
        @Path("id") id: Int
    ): Response<List<Alert>>

    @GET("rota_caminhao/{id}")
    suspend fun getRoutes(
        @Path("id") id: Int
    ): Response<List<Route>>
}