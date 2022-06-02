package com.example.weatherapp.Networking

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiUtilities {


    private var retrofit:Retrofit?=null
    var BASE_URL="https://api.openweathermap.org/data/2.5/"

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()


    fun getApiService():ApiInterface{


        if (retrofit==null){


            retrofit=Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create()).build()


        }

        return retrofit!!.create(ApiInterface::class.java)
    }

}