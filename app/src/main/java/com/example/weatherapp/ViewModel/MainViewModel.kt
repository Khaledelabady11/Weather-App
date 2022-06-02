package com.example.weatherapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.Model.LocationModel
import com.example.weatherapp.Model.ModelClass

class MainViewModel() : ViewModel() {
    val Repo = Repository()
     var mutableLiveData: MutableLiveData<ModelClass>? = null
    var cityLiveData: MutableLiveData<ModelClass>? = null

    fun getCurrentData(latitude:String,longitude:String,api_key:String){
        mutableLiveData=Repo.getMutableLiveData(latitude,longitude,api_key)
    }
    fun getWeatherRepository(): MutableLiveData<ModelClass>? {
        return mutableLiveData
    }
    fun getCityData(cityName:String,api_key: String){
        cityLiveData=Repo.getCityLiveData(cityName,api_key)
    }
    fun getCityWeatherRepository(): MutableLiveData<ModelClass>? {
        return cityLiveData
    }
    override fun onCleared() {
        super.onCleared()
        Repo.completableJob.cancel()
    }
}