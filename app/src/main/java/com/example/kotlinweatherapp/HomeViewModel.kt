package com.example.kotlinweatherapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinweatherapp.database.CurrentDatabaseClass
import com.example.kotlinweatherapp.network.futureforecast.All
import com.example.kotlinweatherapp.network.FutureWeatherItem
import com.example.kotlinweatherapp.database.DataClass
import com.example.kotlinweatherapp.database.FutureDatabaseClass
import com.example.kotlinweatherapp.database.WeatherDataBase
import com.example.kotlinweatherapp.network.CurrentWeatherItem
import com.example.kotlinweatherapp.network.currentweather.CurrentWeatherDataClass
import com.example.kotlinweatherapp.network.futureforecast.FutureWeatherDataClass
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.net.UnknownHostException


class HomeViewModel(
    private val weatherDataBase: WeatherDataBase
): ViewModel() {
    private lateinit var futureListProperties: FutureWeatherDataClass

    var mainText = MutableLiveData<String>()
    var liveCityName = MutableLiveData<String>()

    private var _doesErrorExist = MutableLiveData<Boolean>()
    val doesErrorExist: LiveData<Boolean>
        get() = _doesErrorExist

    private var _all = MutableLiveData<List<All>>()
    val all: LiveData<List<All>>
        get() = _all

    private var _status = MutableLiveData<Status>()
    val status: LiveData<Status>
        get() = _status

    private var _globalAll = MutableLiveData<All>()
    val globalAll: LiveData<All>
        get() = _globalAll

    var time: Long = 0L

    var useDeviceLocation = MutableLiveData<Boolean>()

    private var _currentWeatherInstance = MutableLiveData<CurrentWeatherDataClass>()
    val currentWeatherInstance: LiveData<CurrentWeatherDataClass>
        get() = _currentWeatherInstance

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        uiScope.launch {
            getData()
            time = System.currentTimeMillis()
            getFutureProperties()
        }
        Log.i("ViewModelReal", "mainText value is ${mainText.value} and init called")
        Log.i("ViewModelReal", "livecityname value is ${liveCityName.value} and init called")
    }

    fun insertCityName(){
        uiScope.launch {
            withContext(Dispatchers.IO){
                val data = DataClass(name_text = mainText.value, useDeviceLocation = useDeviceLocation.value)
                weatherDataBase.databaseDao.insertString(data)
            }
        }
    }

    private fun getData(){
        uiScope.launch {
            mainText.value = suspendGetData()
            cityName = mainText.value!!
        }
    }

    private suspend fun suspendGetData(): String{
        return withContext(Dispatchers.IO){
            weatherDataBase.databaseDao.getFormerString()?.name_text ?: "Hello"
        }
    }

    fun setGlobalAll(all: All){
        _globalAll.value = all
    }

    fun doneNavigateToEachWeather(){
        _globalAll.value = null
    }

    fun getFutureProperties() {
        uiScope.launch {

            _status.value = Status.LOADING
            Log.i("ViewModelReal", "cityName is $cityName")
            val getDeferredFutureProperties = FutureWeatherItem.futureRetrofitService.getFutureWeatherAsync(name = cityName)

            try {
                futureListProperties = getDeferredFutureProperties.await()

                val futureDataBase = FutureDatabaseClass(future_weather = futureListProperties)
                withContext(Dispatchers.IO) {
                    weatherDataBase.futureDao.insertFutureAllClass(futureDataBase)
                }

                Log.i("ViewModel", "Add futureDataClass to Room successfully")

                _doesErrorExist.value = false
                _all.value = futureListProperties.list
                _status.value = Status.DONE

                liveCityName.value = futureListProperties.city.name
                Log.i("ViewModelReal", "liveCityName value = ${liveCityName.value} and listProperties.city.name value = ${futureListProperties.city.name}")
            }
            catch (noInternetError: UnknownHostException){
                Log.e("ViewModelReal", "$noInternetError")
                liveCityName.value = "$noInternetError"
                _doesErrorExist.value = true
                _status.value = Status.NO_INTERNET

            }
            catch (locationError: HttpException){
                _doesErrorExist.value = true
                _status.value = Status.LOCATION_ERROR
            }
        }
    }



    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
/*
java.net.UnknownHostException: Unable to resolve host "api.openweathermap.org": No address associated with hostname
2020-06-30 11:06:49.037 5659-5659/com.example.settingsapp E/ViewModelReal: retrofit2.HttpException: HTTP 404 Not Found
 */