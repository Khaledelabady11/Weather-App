package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.TransitionDrawable
import android.inputmethodservice.InputMethodService
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.telecom.Call
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.SharedPreferencesCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.Model.ModelClass

import com.example.weatherapp.ViewModel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.jar.Manifest
import kotlin.math.log
import kotlin.math.roundToInt

const val api_key = "dab3af44de7d24ae7ff86549334e45bd" // HERE PUT YOUR OPENWEATHERMAP API KEY

class MainActivity : AppCompatActivity() {
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    val PERMISSION_ID = 42
    lateinit var sharedPreferences: SharedPreferences


    var lastLocation: Location?=null
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    var mainViewModel: MainViewModel? = null
//     var lati:String=""
//     var longi:String=""



    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)





        // rl_main_layout.visibility=View.GONE

     //   getCurrentLocation()
       // pd_loading.visibility=View.VISIBLE

        getCurrentLocation()


     et_get_city_name.setOnEditorActionListener ({ v, actionId, keyEvent ->
         if (actionId==EditorInfo.IME_ACTION_SEARCH){
             getCityWeather(et_get_city_name.text.toString(), api_key)
             val view =this.currentFocus
             if (view !=null){
                 val imm:InputMethodManager=getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                 imm.hideSoftInputFromWindow(view.windowToken,0)
                 et_get_city_name.clearFocus()

             }
             true
         }
         else false
     })



   }
//    private fun setActualUnit(): String {
//        return when (sharedPreferences.getUnit()) {
//            "metric" -> 0x00B0.toChar() + "C"
//            "imperial" -> 0x00B0.toChar() + "F"
//            else -> "K"
//        }
//    }
    fun Double.kelvinToCelsius() : Int {

        return  (this - 273.15).toInt()
    }
    fun getCityWeather(cityName:String,API_Key:String){
        mainViewModel!!.getCityData(cityName, api_key)
        mainViewModel!!.getCityWeatherRepository()?.observe(this, androidx.lifecycle.Observer { it->
            val sdf=SimpleDateFormat("dd/MM/yyyy hh:mm")
            val currentDate=sdf.format(Date())
            tv_date_time.text=currentDate
            if (it!!.weather[0].main=="clouds"){
                val transition= ResourcesCompat.getDrawable(
                    this.resources,
                    R.drawable.clouds,
                    null
                ) as TransitionDrawable

                iv_weather_icon.setImageDrawable(transition)
            }

           // else if (it!!.weather[0].main=="clouds")
            tv_day_max_temp.text="Day "+kelvinToCelsius(it!!.main.temp_max)+"°"
            tv_day_min_temp.text="Night "+kelvinToCelsius(it!!.main.temp_min)+"°"
            tv_temp.text=""+kelvinToCelsius(it!!.main.temp).toInt()+"°"
            tv_feels_like.text="Feels Like "+kelvinToCelsius(it!!.main.feels_like).toInt()+"°"
            tv_weather_type.text=it!!.weather[0].main

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sunrise.text=timeStampToDate(it!!.sys.sunrise.toLong())
                sunset.text=timeStampToDate(it!!.sys.sunset.toLong())
            }
            //    iv_weather_icon.setImageResource(it.weather[0].icon)
            pressure.text=it!!.main.pressure.toString()
            humidity.text=it!!.main.humidity.toString()+"%"
            wind_speed.text=it!!.wind.speed.toString()+" m/s"
            temp_farenhite.text=""+((kelvinToCelsius(it!!.main.temp)).times(1.8).plus(32).roundToInt())
            // ed_get_city_name.setText(it!!.name)
            city.text=it!!.name

        })
    }


    fun setDataView(lati:String,longi:String){

         pd_loading.visibility=View.VISIBLE

        mainViewModel!!.getCurrentData(lati, longi, api_key)
        mainViewModel!!.getWeatherRepository()?.observe(this, androidx.lifecycle.Observer { it->
            val sdf=SimpleDateFormat("dd/MM/yyyy hh:mm")
            val currentDate=sdf.format(Date())
            tv_date_time.text=currentDate
            tv_day_max_temp.text="Day "+kelvinToCelsius(it!!.main.temp_max)+"°"
            tv_day_min_temp.text="Night "+kelvinToCelsius(it!!.main.temp_min)+"°"
            tv_temp.text=""+kelvinToCelsius(it!!.main.temp).toInt()+"°"
            tv_feels_like.text="Feels Like "+kelvinToCelsius(it!!.main.feels_like).toInt()+"°"
            tv_weather_type.text=it!!.weather[0].main
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sunrise.text=timeStampToDate(it!!.sys.sunrise.toLong())
                sunset.text=timeStampToDate(it!!.sys.sunset.toLong())
            }
        //    iv_weather_icon.setImageResource(it.weather[0].icon)
            pressure.text=it!!.main.pressure.toString()
            humidity.text=it!!.main.humidity.toString()+"%"
            wind_speed.text=it!!.wind.speed.toString()+" m/s"
            temp_farenhite.text=""+((kelvinToCelsius(it!!.main.temp)).times(1.8).plus(32).roundToInt())
           // ed_get_city_name.setText(it!!.name)
            city.text=it!!.name

        })
    }
    private fun getCurrentLocation(){



        if(checkpermission()){

            if (isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    requesstPermission()
                    return
                }
                mFusedLocationClient.lastLocation.addOnCompleteListener(this){ task->
                    val location:Location?=task.result
                    if (location!=null){
                        setDataView(location.latitude.toString(),location.longitude.toString())
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
                android.Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
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
                            android.Manifest.permission.ACCESS_FINE_LOCATION) ===
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

}