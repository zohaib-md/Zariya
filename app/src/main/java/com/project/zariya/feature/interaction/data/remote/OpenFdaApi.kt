package com.project.zariya.feature.interaction.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApi {

    @GET("drug/label.json")
    suspend fun searchDrugLabel(
        @Query("search") search: String,
        @Query("limit") limit: Int = 5
    ): Response<OpenFdaResponse>

    @GET("drug/event.json")
    suspend fun searchAdverseEvents(
        @Query("search") search: String,
        @Query("limit") limit: Int = 10
    ): Response<AdverseEventResponse>
}
