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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }
}
