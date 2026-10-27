package com.example.weatherapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // by lazy - created this only when it is first accessed
    // If weatherApiService is never used, it's never created - save memory
    // Once created, the same instance is reused - no rebuilding
    val weatherApiService: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConstants.WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            // automatic JSON-to-data-class conversion when the API
            // returns JSON, Gson maps it to WeatherResponse
            .build()
            .create(WeatherApiService::class.java)
            // retrofit generates the real HTTP implementation
            // tells retrofit which interface to implement
    }
}