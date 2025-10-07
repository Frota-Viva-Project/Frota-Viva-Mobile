package com.mobile.frotaviva_mobile.api

import com.mobile.frotaviva_mobile.model.Route
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("rota/caminhao/{id}")
    suspend fun getRoutes(
        @Path("id") id: Int
    ): Response<List<Route>>
}