package com.example.kotlinweatherapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlinweatherapp.database.WeatherDataBase

class CurrentWeatherViewModelFactory(
    private val dataBase: WeatherDataBase
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrentWeatherViewModel::class.java)) {
            return CurrentWeatherViewModel(dataBase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}