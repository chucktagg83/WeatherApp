package com.example.weatherapp.data

// represents the JSON body we SEND to APIDog
// Gson converts this kotlin object into JSON automatically when we POST
data class FeedbackRequest(
    val city: String,
    val rating: Int,
    val comment: String
)

