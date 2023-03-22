package br.com.joaovitorqueiroz.weatherapp

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.com.joaovitorqueiroz.weatherapp.core.network.OpenWeatherService
import br.com.joaovitorqueiroz.weatherapp.core.network.factory.openWeatherKey
import br.com.joaovitorqueiroz.weatherapp.util.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationUserEnabled()
    }

    override fun onRestart() {
        super.onRestart()
        checkLocationUserEnabled()
    }

    private fun checkLocationUserEnabled(): String {
        return if (!isLocationEnabled()) {
            showToastWithText(getString(R.string.message_check_location_is_disabled))
            val intent = Intent()
                .setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            getString(R.string.message_check_location_is_disabled)
        } else {
            showToastWithText(getString(R.string.message_check_location_is_enabled))
            verifyPermissionLocation()
            getString(R.string.message_check_location_is_enabled)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showToastWithText(text: String) {
        Toast.makeText(
            applicationContext,
            text,
            Toast.LENGTH_SHORT,
        ).show()
    }

    private fun verifyPermissionLocation() {
        val locationPermissionsListener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    if (report.areAllPermissionsGranted()) {
                        requestLocationData()
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        showToastWithText(getString(R.string.message_permission_location_denied))
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?,
            ) {
                showRationalDialogForPermissionsSettings(
                    getString(R.string.rationale_dialog_message),
                )
            }
        }

        Dexter
            .withContext(applicationContext)
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            .withListener(locationPermissionsListener)
            .onSameThread()
            .check()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000,
        ).build()

        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.getMainLooper(),
        )
    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {
        if (Constants.isNetworkAvailable(applicationContext)) {
            val service = OpenWeatherService.service
            lifecycleScope.launch(Dispatchers.IO) {
                val response =
                    service.getWeather(latitude, longitude, Constants.METRIC_UNIT, openWeatherKey)
                response.body()?.let { safeResponse ->
                    Log.e("Message", response.message())
                    Log.e("Weather Response", safeResponse.toString())
                }
            }
        } else {
            showToastWithText(getString(R.string.message_no_internet_connection))
        }
    }

    private fun showRationalDialogForPermissionsSettings(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.rationale_dialog_positive_button_text)) { _, _ ->
                try {
                    val uri = Uri.fromParts("package", packageName, null)
                    val intent = Intent()
                        .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(uri)

                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton(
                getString(R.string.rationale_dialog_negative_button_text),
            ) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            mLastLocation?.let { safeLocation ->
                val latitude = safeLocation.latitude
                Log.i(TAG_CURRENT_LATITUDE, "$latitude")
                val longitude = safeLocation.longitude
                Log.i(TAG_CURRENT_LONGITUDE, "$longitude")
                getLocationWeatherDetails(latitude, longitude)
            }
        }
    }

    companion object {
        const val TAG_CURRENT_LATITUDE = "Current Latitude"
        const val TAG_CURRENT_LONGITUDE = "Current Longitude"
    }
}
