package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Class 1
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.example.weatherapp.network.AppConstants
import com.example.weatherapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import kotlin.coroutines.cancellation.CancellationException
// Class 2
import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
// Class 3
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import com.example.weatherapp.data.FeedbackRequest
// Class 4
import androidx.compose.material3.OutlinedButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WeatherScreen()
                }
            }
        }
    }
}

@Composable
fun WeatherScreen() {

    var city by remember { mutableStateOf("") }
    var cityResult by remember { mutableStateOf("City: --") }
    var tempResult by remember { mutableStateOf("Temperature: --") }
    var descResult by remember { mutableStateOf("Description: --") }

    var windResult by remember { mutableStateOf("Wind Speed: --") }
    var humidityResult by remember { mutableStateOf("Humidity: --")}
    var isLoading by remember { mutableStateOf(false)}

    var currentCity by remember {mutableStateOf("") }
    var rating by remember { mutableStateOf(3) }
    var comment by remember { mutableStateOf("") }
    var feedbackResult by remember { mutableStateOf("") }
    var feedbackLoading by remember { mutableStateOf(false) }

    var recentSearches by remember { mutableStateOf(listOf<String>()) }

    val context = LocalContext.current // Toast.makeText() requires an Android Context -- a composable isn't an Activity, so we pull one from LocalContext insted
    val scope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Enter city name") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            enabled = !isLoading, // disables the button mid-request
            onClick = {
                val trimmedCity = city.trim()
                if (trimmedCity.isEmpty()) {
                    Toast.makeText(
                        context, "Please enter a city name", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    isLoading = true

                    // Assignment 4
                    // 1. Declare "var feedbackLoading by remember { mutableStateOf(false) }"
                    //    near the top of WeatherScreen(), next to the other state variables
                    // 2. Set feedbackLoading = true right before the feedback scope.launch { }
                    // 3. Set feedbackLoading = false inside a finally { } wrapped around the
                    //    feedback try/catch -- same shape as Get Weather's finally block
                    // 4. On the Submit Feedback Button: add enabled = !feedbackLoading,
                    //    and swap its Text to "Submitting..." while feedbackLoading is true
                    scope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                RetrofitClient.weatherApiService.getWeather(
                                    city = trimmedCity,
                                    apiKey = AppConstants.API_KEY,
                                    units = AppConstants.UNITS
                                )
                            }
                            // response.raw() gives the underlying OkHttp response
                            Log.d("WeatherApp", "Requested URL: ${response.raw().request.url}")
                            // the exact URL Retrofit built.  check log cat filter by WeatherApp
                            Log.d("WeatherApp", "Response code: ${response.code()}")
                            if (response.isSuccessful) {
                                val weatherResponse = response.body()
                                if (weatherResponse != null) {
                                    cityResult = "City: ${weatherResponse.name} "
                                    tempResult = "Temperature: ${weatherResponse.main.temp}°F"
                                    descResult =
                                        "Description: ${weatherResponse.weather.firstOrNull()?.description ?: "Unavailable"}"
                                    // Assignment 2
                                    windResult = "Wind Speed: ${weatherResponse.wind.speed}m/s"
                                    humidityResult = "Humidity: ${weatherResponse.main.humidity}%"
                                    // 1. Set windResult from weather.wind.speed (append " m/s")
                                    // 2. Set humidityResult from weather.main.humidity (append "%")
                                    // 3. Below in the else branch, replace the single Toast with a
                                    //    when (response.code()) -- see Hour 3 for the exact messages
                                    // TODO: this Toast gets replaced by step 3 above -- a when (response.code())
                                    // with different messages for 404, 401, and anything else
                                    currentCity = trimmedCity // remember this city so feedback can POST
                                    recentSearches = (listOf(weatherResponse.name) + recentSearches.filter { it != weatherResponse.name}).take(5)
                                }
                            } else {
                                val errorMessage = when (response.code()) {
                                    404 -> "City not found.  Check the name and try again."
                                    401 -> "Invalid API key.  Check your configuration."
                                    else -> "Weather request failed.  Please try again."
                                }
                                Toast.makeText(
                                    context,
                                    "City not found. Check the name and try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            Toast.makeText(
                                context, "Network error. Check your connection.", Toast.LENGTH_SHORT
                            ).show()
                        }
                        finally { // finally always runs - success, HTTP error, or exception
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text( if (isLoading) "Loading..." else "Get Weather")
        }
        Text(
            text = cityResult, fontSize = 20.sp, modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = tempResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = descResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = windResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = humidityResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp)
        )

        // Cache control + recent searches
        OutlinedButton(
            onClick = {
                RetrofitClient.clearCache()
                    Toast.makeText(context, "Cache Cleared", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Clear Cache")
        }

        if (recentSearches.isNotEmpty()){
            Text("Recent Searches", fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp)) {
                recentSearches.forEach { recentCity ->
                    AssistChip(
                        onClick = { city = recentCity },
                        label = { Text(recentCity) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

        }

        HorizontalDivider( modifier = Modifier.padding(top = 24.dp, bottom = 16.dp))

        Text("How do you feel about today's weather?", fontSize = 16.sp)

        // rating system
        Slider(
            value = rating.toFloat(),
            onValueChange = { rating = it.toInt() },
            valueRange = 1f..5f,
            steps = 3
        )

        Text("Rating: $rating/5") // slider has no built-in label

        TextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Leave a comment about the weather...")},
            minLines = 3,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Button(
            enabled = !feedbackLoading,
            onClick = {
                if (currentCity.isEmpty()) {
                    Toast.makeText(context, "Please fetch weather for a city first", Toast.LENGTH_SHORT).show()
                } else if (comment.isBlank()) {
                    Toast.makeText(context, "Please leave a comment", Toast.LENGTH_SHORT).show()
                } else {
                    feedbackLoading = true

                    scope.launch {
                        try {
                            val request = FeedbackRequest(city = currentCity, rating = rating, comment = comment)
                            val response = withContext(Dispatchers.IO) {
                                RetrofitClient.feedbackAPIService.submitFeedback(request)
                            }
                            // ASSIGNMENT 3
                            // 1. If response.isSuccessful: set feedbackResult to a success message,
                            //    then clear the comment field and reset rating back to 3
                            // 2. If NOT successful: set feedbackResult to a failure message
                            if (response.isSuccessful) {
                                feedbackResult = "Feedback submitted successfully!"
                                comment = ""
                                rating = 3
                            } else {
                                feedbackResult = "Failed to submit feedback. Please try again."
                            }
                        } catch (e: Exception){
                            if (e is CancellationException) throw e

                            feedbackResult = "Error submitting feedback.  Check your connection."
                        } finally {
                            feedbackLoading = false
                        }
                    }
                }
            }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(
                if (feedbackLoading) "Submitting..."
                else "Submit Feedback"
            )
        }
        Text(feedbackResult, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
    }
}
