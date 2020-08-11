package com.example.kotlinweatherapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinweatherapp.database.CurrentDatabaseClass
import com.example.kotlinweatherapp.database.WeatherDataBase
import com.example.kotlinweatherapp.network.CurrentWeatherItem
import com.example.kotlinweatherapp.network.currentweather.CurrentWeatherDataClass
import kotlinx.coroutines.*
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException

class CurrentWeatherViewModel(private val weatherDataBase: WeatherDataBase) : ViewModel() {

    private var _currentWeatherInstance = MutableLiveData<CurrentWeatherDataClass>()
    val currentWeatherInstance: LiveData<CurrentWeatherDataClass>
        get() = _currentWeatherInstance

    private var _status = MutableLiveData<Status>()
    val status: LiveData<Status>
        get() = _status

    val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun getCurrentProperties(){
        uiScope.launch {
            _status.value = Status.LOADING
            Timber.d("cityName is $cityName")
            val getDeferredFutureProperties =
                CurrentWeatherItem.currentRetrofitService.getCurrentWeatherAsync(name = cityName)

            try {
                _currentWeatherInstance.value = getDeferredFutureProperties.await()
                val currentDatabase =
                    CurrentDatabaseClass(current_weather = _currentWeatherInstance.value!!)
                withContext(Dispatchers.IO) {
                    weatherDataBase.currentDao.insertCurrentDataClass(currentDatabase)
                }
                Timber.d("Add currentDataClass to Room successfully")
                _status.value = Status.DONE
            }
            catch (noInternetError: UnknownHostException){
                _status.value = Status.NO_INTERNET

            }
            catch (locationError: HttpException){
                _status.value = Status.LOCATION_ERROR
            }
            catch(t: Throwable){
                Log.e("ViewModelCurrent", "Value of cityName is $cityName")
                Log.e("ViewModelCurrent", "$t")
            }
        }
    }
}