package com.example.kotlinweatherapp


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.kotlinweatherapp.database.WeatherDataBase
import com.example.kotlinweatherapp.databinding.CurrentWeatherFragmentBinding
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class CurrentWeatherFragment: Fragment() {

    lateinit var binding: CurrentWeatherFragmentBinding
    lateinit var viewModel: CurrentWeatherViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.current_weather_fragment, container, false)

        val application = requireNotNull(this.activity).application
        val dataBase = WeatherDataBase.getInstance(application)

        viewModel = ViewModelProvider(this, CurrentWeatherViewModelFactory(dataBase!!)).get(CurrentWeatherViewModel::class.java)

        viewModel.getCurrentProperties()

        viewModel.currentWeatherInstance.observe(viewLifecycleOwner, Observer {
            currentInstance()
        })

        binding.lifecycleOwner = this


        return binding.root
    }

    @SuppressLint("SetTextI18n")
    fun currentInstance() {
        if (viewModel.currentWeatherInstance.value == null){
            Timber.d("viewModel.currentWeatherInstance.value is null")
            return
        }

        val currentWeatherInstance = viewModel.currentWeatherInstance.value

        binding.let {

            val main = currentWeatherInstance!!.main
            val cloudCover = currentWeatherInstance.clouds
            val weather = currentWeatherInstance.weather[0]
            val wind = currentWeatherInstance.wind

            it.currentWeatherImage.setImageResource(
                when (weather.description) {
                    "clear sky" -> R.drawable.sunny
                    "light rain" -> R.drawable.scattered_showers
                    else -> R.drawable.partly_cloudy
                }
            )

            it.currentTempDisplayText.text = "${(main.temp - 273.15).toInt()}"

            it.currentFeelsLikeText.text = "Feels like ${(main.temp - 273.15).toInt()}°"

            it.currentWeatherStatusDisplayText.text = weather.description

            val cloudPercent = (cloudCover.all * 8 / 100)
            it.currentCloudsDisplayText.text =
                if (cloudPercent == 1) "$cloudPercent Okta" else "$cloudPercent Oktas"

            val speed = (wind.speed * 1.852).toInt()
            it.currentWindDisplayText.text = "$speed km/h"

            it.currentPressureDisplayText.text = "${main.pressure} mBars"

            it.currentHumidityDisplayText.text = "${main.humidity}%"
        }
        binding.currentDateText.text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(System.currentTimeMillis())
    }
}