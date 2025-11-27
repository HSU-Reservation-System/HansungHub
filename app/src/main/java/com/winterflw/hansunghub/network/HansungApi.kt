package com.winterflw.hansunghub.network

import com.winterflw.hansunghub.network.model.DisabledTimesResponse
import com.winterflw.hansunghub.network.model.LoginRequest
import com.winterflw.hansunghub.network.model.LoginResponse
import com.winterflw.hansunghub.network.model.ReserveRequest
import com.winterflw.hansunghub.network.model.ReserveResponse
import com.winterflw.hansunghub.network.model.SpacesResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HansungApi {

    @POST("/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

    @POST("/reserve")
    suspend fun reserve(
        @Body req: ReserveRequest
    ): ReserveResponse

    @GET("/spaces")
    suspend fun getSpaces(): SpacesResponse

    @GET("/disabled-times")
    suspend fun getDisabledTimes(
        @Query("date") date: String,
        @Query("spaceSeq") spaceSeq: Int
    ): DisabledTimesResponse



}
