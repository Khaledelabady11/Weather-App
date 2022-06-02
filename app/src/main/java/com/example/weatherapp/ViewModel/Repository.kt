package com.example.weatherapp.ViewModel

import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.MainActivity
import com.example.weatherapp.Model.LocationModel
import com.example.weatherapp.Model.ModelClass
import com.example.weatherapp.Networking.ApiInterface
import com.example.weatherapp.Networking.ApiUtilities
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
const val api_key = "dab3af44de7d24ae7ff86549334e45bd" // HERE PUT YOUR OPENWEATHERMAP API KEY

class Repository {

   // private var Data = mutableListOf<ModelClass>()
    //var latitude:St = 0.0
     var mutableLiveData = MutableLiveData<ModelClass>()
    var cityLiveData = MutableLiveData<ModelClass>()

    val completableJob = Job()
//    companion object {
//        private var weatherRepository: Repository? = null
//        fun getInstance(): Repository {
//            if (weatherRepository == null) {
//                weatherRepository = Repository()
//            }
//            return weatherRepository as Repository
//        }
//    }

    fun getMutableLiveData( latitude:String ,longitude: String,api_key:String):MutableLiveData<ModelClass> {
        ApiUtilities.getApiService().getCurrentWeatherData(latitude,longitude, api_key).enqueue(object : Callback<ModelClass>{
            override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                mutableLiveData.value=response.body()

            }

            override fun onFailure(call: Call<ModelClass>, t: Throwable) {

            }
        })
        return mutableLiveData;

    }
    fun getCityLiveData( cityName: String,api_key:String):MutableLiveData<ModelClass> {
        ApiUtilities.getApiService().getCityWeatherData(cityName, api_key).enqueue(object : Callback<ModelClass>{
            override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                cityLiveData.value=response.body()

            }

            override fun onFailure(call: Call<ModelClass>, t: Throwable) {

            }
        })
        return cityLiveData;

    }

}