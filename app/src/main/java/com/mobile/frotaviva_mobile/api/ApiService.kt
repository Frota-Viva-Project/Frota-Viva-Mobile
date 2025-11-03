package com.mobile.frotaviva_mobile.api

import com.mobile.frotaviva_mobile.auth.LoginResponse
import com.mobile.frotaviva_mobile.auth.TokenExchangeRequest
import com.mobile.frotaviva_mobile.model.Maintenance
import com.mobile.frotaviva_mobile.model.Alert
import com.mobile.frotaviva_mobile.model.AlertRequest
import com.mobile.frotaviva_mobile.model.DriverRequest
import com.mobile.frotaviva_mobile.model.LocationUpdateRequest
import com.mobile.frotaviva_mobile.model.MaintenanceRequest
import com.mobile.frotaviva_mobile.model.Meter
import com.mobile.frotaviva_mobile.model.Notification
import com.mobile.frotaviva_mobile.model.Route
import com.mobile.frotaviva_mobile.model.TokenRegistrationRequest
import com.mobile.frotaviva_mobile.model.TruckResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("/v1/api/auth/firebase")
    suspend fun exchangeFirebaseToken(
        @Body request: TokenExchangeRequest
    ): Response<LoginResponse>

    @PATCH("rota_caminhao/finalizada")
    suspend fun markRouteAsDone(
        @Query("id_caminhao") idCaminhao: Int,
        @Query("id_rotacaminhao") idRota: Int
    ): Response<Unit>

    @PATCH("manutencao/caminhao")
    suspend fun markMaintenanceAsDone(
        @Query("id_caminhao") idCaminhao: Int,
        @Query("id_manuntecao") idManutencao: Int
    ): Response<Unit>

    @PATCH("manutencao/caminhao/servico")
    suspend fun askServiceForMaintenance(
        @Query("id_caminhao") idCaminhao: Int,
        @Query("id_manuntecao") idManutencao: Int
    ): Response<Unit>

    @PATCH("alerta")
    suspend fun markAlertAsDone(
        @Query("id_caminhao") idCaminhao: Int,
        @Query("id_alerta") idAlerta: Int
    ): Response<Unit>

    @PATCH("alerta/servico")
    suspend fun markAlertForMaintenance(
        @Query("id_caminhao") idCaminhao: Int,
        @Query("id_alerta") idAlerta: Int
    ): Response<Unit>

    @POST("motorista/{id_motorista}")
    suspend fun linkDriver(
        @Query("cod_empresa") codEmpresa: String,
        @Body request: DriverRequest
    ): Response<TruckResponse>

    @PUT("fcm/register")
    suspend fun registerToken(@Body request: TokenRegistrationRequest): Response<Void>

    @GET("fcm/history/{userId}")
    suspend fun getNotificationHistory(@Path("userId") userId: Int): Response<List<Notification>>
}