package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.Model.ModelClass
import com.example.weatherapp.Utilities.ApiUtilities
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    //private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
   // private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
       // rl_main_layout.visibility=View.GONE

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()
    }


    private fun getCurrentLocation(){



        if(checkpermission()){

            if (isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                  requesstPermission()
                    return
                }
                fusedLocationClient.lastLocation.addOnCompleteListener(this){ task->
                    val location:Location?=task.result
                    if (location!=null){
                       fetchCurrentLocationWeather(location.latitude.toString(),location.longitude.toString())
                    }


                }
            }
            else
            {
                //open settings to open location
                Toast.makeText(this, "Turn On Location", Toast.LENGTH_SHORT).show()
                val intent=Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        else
        {
            //request permission
            requesstPermission()

        }

    }

    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {
        ApiUtilities.getApiService().getCurrentWeatherData(latitude,longitude, API_KEY).enqueue(object : Callback<ModelClass>{
            override fun onResponse(call: Call<ModelClass>?, response: Response<ModelClass>?) {

                if (response!!.isSuccessful){
                    response.body().let { Log.d("here", it.name) }
                    val sdf=SimpleDateFormat("dd/MM/yyyy hh:mm")
                    val currentDate=sdf.format(Date())
                    tv_date_time.text=currentDate
                    tv_day_max_temp.text="Day "+kelvinToCelsius(response.body().main.temp_max)+"°"
                    tv_day_min_temp.text="Night "+kelvinToCelsius(response.body().main.temp_min)+"°"
                    tv_temp.text=""+kelvinToCelsius(response.body().main.temp)+"°"
                    tv_feels_like.text="Feels Like "+kelvinToCelsius(response.body().main.feels_like)+"°"
                    tv_weather_type.text=response.body().weather[0].main
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sunrise.text=timeStampToDate(response.body().sys.sunrise.toLong())
                        sunset.text=timeStampToDate(response.body().sys.sunset.toLong())
                    }
                    pressure.text=response.body().main.pressure.toString()
                    humidity.text=response.body().main.humidity.toString()+"%"
                    wind_speed.text=response.body().wind.speed.toString()+" m/s"
                    temp_farenhite.text=""+((kelvinToCelsius(response.body().main.temp)).times(1.8).plus(32).roundToInt())
                    ed_get_city_name.setText(response.body().name)

                }
            }

            override fun onFailure(call: Call<ModelClass>?, t: Throwable?) {
                Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun setDataViews(body: ModelClass?) {
        val sdf=SimpleDateFormat("dd/mm/yyyy hh:mm")
        val currentDate=sdf.format(Date())
        tv_date_time.text=currentDate
         tv_day_max_temp.text="Day "+kelvinToCelsius(body!!.main.temp_max)+"°"
        tv_day_min_temp.text="Night "+kelvinToCelsius(body!!.main.temp_min)+"°"
        tv_temp.text=""+kelvinToCelsius(body!!.main.temp)+"°"
        tv_feels_like.text="Feels Like "+kelvinToCelsius(body!!.main.feels_like)+"°"
        tv_weather_type.text=body!!.weather[0].main
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sunrise.text=timeStampToDate(body.sys.sunrise.toLong())
            sunset.text=timeStampToDate(body.sys.sunset.toLong())
        }
        pressure.text=body!!.main.pressure.toString()
        humidity.text=body!!.main.humidity.toString()+"%"
        wind_speed.text=body!!.wind.speed.toString()+" m/s"
        temp_farenhite.text=""+((kelvinToCelsius(body.main.temp)).times(1.8).plus(32).roundToInt())
        ed_get_city_name.setText(body!!.name)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeStampToDate(timeStamp:Long):String{
        val localTime=timeStamp.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()
    }


    private fun kelvinToCelsius(temp:Double):Double{
        var intTemp=temp
        intTemp=intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1,RoundingMode.UP).toDouble()

    }

    private fun isLocationEnabled():Boolean{
        val locationManager:LocationManager= getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        //if GPS_PROVIDER or NETWORK_PROVIDER is enabled return true else return false
    }

    private fun requesstPermission() {
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
    }




    private fun checkpermission(): Boolean {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
            &&ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
        )
        {
            return true
        }
        return false

    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val API_KEY="dab3af44de7d24ae7ff86549334e45bd"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                        getCurrentLocation()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }







    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }



        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
//
            }
        }
    }


    }