package com.example.weatherapp.network

import com.example.weatherapp.data.FeedbackRequest
import retrofit2.Response
import retrofit2.http.Body // send this object as the JSON request body
import retrofit2.http.POST // HTTP POST method


interface FeedbackApiService {

    @POST("feedback")
    // @POST tells Retrofit: make a POST request to FEEDBACK_BASE_URL + "feedback"
    // This matches the /feedback endpoint we created in APIDog
    suspend fun submitFeedback(
        @Body request: FeedbackRequest
    ): Response<Unit>
    // @Body = Gson converts our FeedbackRequest object to JSON
    // and places it in the HTTP request body
    // This is the key difference from @Query (which adds ?key=value to the URL)
}