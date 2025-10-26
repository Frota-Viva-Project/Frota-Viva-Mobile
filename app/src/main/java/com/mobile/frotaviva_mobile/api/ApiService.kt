package com.mobile.frotaviva_mobile.api

import com.mobile.frotaviva_mobile.auth.LoginResponse
import com.mobile.frotaviva_mobile.auth.TokenExchangeRequest
import com.mobile.frotaviva_mobile.model.Maintenance
import com.mobile.frotaviva_mobile.model.Alert
import com.mobile.frotaviva_mobile.model.AlertRequest
import com.mobile.frotaviva_mobile.model.LocationPostResponse
import com.mobile.frotaviva_mobile.model.LocationUpdateRequest
import com.mobile.frotaviva_mobile.model.MaintenanceRequest
import com.mobile.frotaviva_mobile.model.Meter
import com.mobile.frotaviva_mobile.model.Route
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("arduino/{id}")
    suspend fun getMeters(
        @Path("id") id: Int
    ): Response<Meter>

    @POST("manutencao/caminhao/{id}")
    suspend fun sendMaintenance(
        @Path("id") id: Int,
        @Body request: MaintenanceRequest
    ): Response<Unit>

    @POST("alerta/{id}")
    suspend fun sendAlert(
        @Path("id") id: Int,
        @Body request: AlertRequest
    ): Response<Unit>

    @POST("maps")
    suspend fun postLocation(
        @Body request: LocationUpdateRequest
    ): Response<Int>

    @PUT("maps/{idMaps}")
    suspend fun putLocation(
        @Path("idMaps") idMaps: String,
        @Body request: LocationUpdateRequest
    ): Response<Unit>

    @POST("/api/auth/firebase")
    suspend fun exchangeFirebaseToken(
        @Body request: TokenExchangeRequest
    ): Response<LoginResponse>
}