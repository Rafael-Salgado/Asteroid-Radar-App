package com.udacity.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Constants.BASE_URL
import kotlinx.coroutines.Deferred
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

interface NeoWsApi {
    @GET("neo/rest/v1/feed?")
    suspend fun getAsteroids(@Query("start_date")startDate: String, @Query("api_key")key: String): String

    @GET("planetary/apod?")
    suspend fun getTodayImage(@Query("api_key")key: String): String
}

object NeoApi {
    val retrofitService: NeoWsApi by lazy { retrofit.create(NeoWsApi::class.java) }
}