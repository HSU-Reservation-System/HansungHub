package com.winterflw.hansunghub.network

import com.winterflw.hansunghub.network.HansungApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://43.203.173.74:8000"

    val api: HansungApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HansungApi::class.java)
    }
}
