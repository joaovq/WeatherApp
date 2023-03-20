package br.com.joaovitorqueiroz.weatherapp

import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkLocationUserEnabled()
    }

    private fun checkLocationUserEnabled(): String {
        if (!isLocationEnabled()) {
            Toast.makeText(
                applicationContext,
                "Your location provider is turned off. Please turn it on",
                Toast.LENGTH_SHORT,
            ).show()
            val intent = Intent()
                .setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            return "Your location provider is turned off. Please turn it on"
        } else {
            Toast.makeText(
                applicationContext,
                "Your location provider is already ON",
                Toast.LENGTH_SHORT,
            ).show()
            return "Your location provider is already ON"
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
