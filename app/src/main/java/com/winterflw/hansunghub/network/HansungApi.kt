package com.winterflw.hansunghub.network

import com.winterflw.hansunghub.network.model.DisabledTimesResponse
import com.winterflw.hansunghub.network.model.LoginRequest
import com.winterflw.hansunghub.network.model.LoginResponse
import com.winterflw.hansunghub.network.model.ReserveRequest
import com.winterflw.hansunghub.network.model.ReserveResponse
import com.winterflw.hansunghub.network.model.SpacesResponse
import com.winterflw.hansunghub.network.model.UserInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HansungApi {

    // 코딩라운지 로그인
    @POST("/codinglogin")
    suspend fun codingLogin(@Body body: LoginRequest): Response<LoginResponse>

    // 학술정보관 로그인
    @POST("/librarylogin")
    suspend fun libraryLogin(@Body body: LoginRequest): Response<LoginResponse>

    // 코딩 라운지 예약
    @POST("/coding-lounge/reserve")
    suspend fun reserve(
        @Body req: ReserveRequest
    ): ReserveResponse

    // 코딩 라운지 공간 목록 조회
    @GET("/coding-lounge/spaces")
    suspend fun getSpaces(): SpacesResponse

    // 코딩 라운지 비활성 시간 조회
    @GET("/coding-lounge/disabled-times")
    suspend fun getDisabledTimes(
        @Query("date") date: String,
        @Query("spaceSeq") spaceSeq: Int
    ): DisabledTimesResponse

    // 사용자 정보 조회
    @GET("/coding-lounge/user-info")
    suspend fun getUserInfo(): UserInfoResponse
}
