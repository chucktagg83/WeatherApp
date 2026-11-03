package com.example.weatherapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


// RetryInterceptor: automatically retries failed network calls
// this is a class (not object) because each Retrofit client will get its own instance
class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    // Interceptor = a piece of code that runs on every request/response
    override fun intercept(chain: Interceptor.Chain) : Response {
        // chain = the request pipeline - calling chain.proceed() executes the request
        var lastException : IOException? = null
        repeat(maxRetries) { attempt ->
            try {                                       // attempts the network call
                return chain.proceed(chain.request())   // if successful: "return" exits the function
            } catch (e: IOException) { // IOException = network failure
                lastException = e
            }
        }
        throw lastException ?: IOException("Request failed after $maxRetries retries")
    }
}

object RetrofitClient {
    // lateinit = will be assigned in init() before any service is accessed
    private lateinit var appContext: Context

    private  lateinit var cache: Cache

    fun init(context: Context) {
        appContext = context.applicationContext
        // .applicationContext = the global app context, not tied to any specific Activity
        // Using applicationContext prevents memory leaks
    }

    fun clearCache() {
        if (::cache.isInitialized) {
            cache.evictAll()
        }
    }

    // by lazy - created this only when it is first accessed
    // If weatherApiService is never used, it's never created - save memory
    // Once created, the same instance is reused - no rebuilding
    val weatherApiService: WeatherApiService by lazy {
        val cacheDir = File(appContext.cacheDir, "http_cache")
            // appContext.cacheDir = the apps private cache directory on disk
            // "http_cache" = subfolder name we choose - keeps HTTp cache separate
        val cache = Cache(cacheDir, 5L * 1024 * 1024)
        // 5L * 1024 * 1024 = 5MB max disc space: L = long type

        val okHttpClient = OkHttpClient.Builder()
            .cache(cache) // tell OkHttp to use our disk cache for GET responses
            .addNetworkInterceptor { chain -> // Network interceptor runs AFTER the network response is received
                chain.proceed(chain.request()).newBuilder()
                    .header("Cache-Control", "public, max-age=300")
                    .build()
            }
            .addInterceptor(RetryInterceptor(maxRetries = 3)) // add retry interceptor - up to 3 attempts
            .connectTimeout(30, TimeUnit.SECONDS) // connectTimeout - max time to establish the TCP connection
            .readTimeout(30, TimeUnit.SECONDS) // max time to wait for data after connection
            .build()

        Retrofit.Builder()
            .baseUrl(AppConstants.WEATHER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            // automatic JSON-to-data-class conversion when the API
            // returns JSON, Gson maps it to WeatherResponse
            .build()
            .create(WeatherApiService::class.java)
            // retrofit generates the real HTTP implementation
            // tells retrofit which interface to implement
    }

    val feedbackAPIService: FeedbackApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor(maxRetries = 3))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(AppConstants.FEEDBACK_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FeedbackApiService::class.java)
    }
}