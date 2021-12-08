package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.location.LocationRequest




class MainActivity : AppCompatActivity() {

    var CITY: String = "Санкт-Петербург"
    val API: String = "542ee8fae9a91e46e63fa703db1f288c" //API key
    var country = Locale.getDefault().country
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val TAG = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val location = getLastKnownLocation();

        Log.d(TAG, location.toString())


        val tv_address: TextView = findViewById < TextView >(R.id.adress)



        val builder = AlertDialog.Builder(this@MainActivity)

        tv_address.setOnClickListener{

            val view: View = LayoutInflater.from(this).inflate(R.layout.custom_alertdialog, null)

            val name_town: EditText = view.findViewById < EditText >(R.id.nameTown)
            builder.setView(view)
                .setIcon(R.drawable.icon_weather)
                .setTitle("Смена локации")
                .setMessage("Введите город, пожалуйста")
                .setPositiveButton("Сменить") {
                        dialog, id ->  dialog.dismiss()
                        CITY = name_town.text.toString()
                        weatherTask().execute()
                }
                .setNegativeButton("Отмена") {
                        dialog, id ->  dialog.dismiss()
                }
                .create()
                .show()
        }

        weatherTask().execute()
    }

    fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                if (location != null) {
                    //получение широты, долготуы и другой информацию из этого местаположения
                }

            }

    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE

        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API&lang=$country").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = getString(R.string.update_at) + " " + SimpleDateFormat("dd.MM.yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                val temp = main.getString("temp")+"°C"
                val tempMin = getString(R.string.min_temp) + " " + main.getString("temp_min")+"°C"
                val tempMax = getString(R.string.max_temp) + " " + main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure") + " " +getString(R.string.pressure_izmer)
                val humidity = main.getString("humidity") + "%"
                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed") + " " + getString(R.string.speed_wind)
                val weatherDescription = weather.getString("description")
                val weatherIcon = weather.getString("icon")

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                /* Заполнение извлеченных данных в представлениях */

                findViewById<TextView>(R.id.adress).text = address
                findViewById<TextView>(R.id.updated_at).text =  updatedAtText

                val weatherView: ImageView = findViewById<ImageView>(R.id.weatherIcon)

                when (weatherIcon) {

                    "01n" -> weatherView.setImageResource(R.drawable.moon)
                    "02n" -> weatherView.setImageResource(R.drawable.mooncloud)
                    "01d" -> weatherView.setImageResource(R.drawable.sun)
                    "02d" -> weatherView.setImageResource(R.drawable.suncloud)
                    "03d" -> weatherView.setImageResource(R.drawable.cloud)
                    "03n" -> weatherView.setImageResource(R.drawable.cloud)
                    "04d" -> weatherView.setImageResource(R.drawable.cloud2)
                    "04n" -> weatherView.setImageResource(R.drawable.cloud2)
                    "09d" -> weatherView.setImageResource(R.drawable.raincloud)
                    "09n" -> weatherView.setImageResource(R.drawable.raincloud)
                    "10d" -> weatherView.setImageResource(R.drawable.sunrain)
                    "10n" -> weatherView.setImageResource(R.drawable.moonrain)
                    "11d" -> weatherView.setImageResource(R.drawable.storm)
                    "11n" -> weatherView.setImageResource(R.drawable.storm)
                    "13d" -> weatherView.setImageResource(R.drawable.snow)
                    "13n" -> weatherView.setImageResource(R.drawable.snow)
                    "50d" -> weatherView.setImageResource(R.drawable.mist)
                    "50n" -> weatherView.setImageResource(R.drawable.mist)
                    else -> {
                        weatherView.setImageResource(R.drawable.suncloud)
                    }

                }

                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }
        }
    }
}
