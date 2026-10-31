package com.example.weatherapp.network
// "object" in Kotlin = a singleton class
// A singleton means there is exactly ONE instance in the whole app
// You never write: val c = AppConstants() -- you just use: AppConstants.API_KEY
// Perfect for constants because you never want two different copies of your API key
object AppConstants {
    // Base URL = the root address of the API
    // Every call starts with this, then add the specific endpoint path
    // IMPORTANT: must end with "/" retrofit requires this
    const val WEATHER_BASE_URL = "https://api.openweathermap.org/"

    // MY API KEY -
    const val API_KEY = "7a54d6620c01dedcaa6d537815c96387"

    // UNITS
    const val UNITS = "imperial"

    // NEW for Class 3: Paste your APIDog Cloud Mock base URL here
    // Example: "https://mock.apidog.com/m1/123456-0-default/"
    // IMPORTANT: must end with "/" -- Retrofit requires it
    const val FEEDBACK_BASE_URL = "https://mock.apidog.com/m1/1407480-1417572-default/"
}
