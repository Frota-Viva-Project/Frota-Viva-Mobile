package com.mobile.frotaviva_mobile.api

import com.mobile.frotaviva_mobile.model.Maintenance
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("manutencao/caminhao/{id}")
    suspend fun getMaintenances(
        @Path("id") id: Int
    ): Response<List<Maintenance>>
}