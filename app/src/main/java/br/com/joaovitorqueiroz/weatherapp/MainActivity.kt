package br.com.joaovitorqueiroz.weatherapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import br.com.joaovitorqueiroz.weatherapp.core.network.OpenWeatherService
import br.com.joaovitorqueiroz.weatherapp.core.network.WeatherService
import br.com.joaovitorqueiroz.weatherapp.core.network.factory.UrlFactory
import br.com.joaovitorqueiroz.weatherapp.core.network.factory.openWeatherKey
import br.com.joaovitorqueiroz.weatherapp.databinding.ActivityMainBinding
import br.com.joaovitorqueiroz.weatherapp.models.WeatherResponse
import br.com.joaovitorqueiroz.weatherapp.util.DatePattern
import br.com.joaovitorqueiroz.weatherapp.util.NetworkConnection
import br.com.joaovitorqueiroz.weatherapp.util.extension.isNightModeSystemDefault
import br.com.joaovitorqueiroz.weatherapp.util.extension.startAnimationFromId
import br.com.joaovitorqueiroz.weatherapp.util.extension.unixTimeFormat
import br.com.joaovitorqueiroz.weatherapp.util.preferences.UserPrefs
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var mProgressDialog: Dialog
    private lateinit var service: WeatherService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        service = OpenWeatherService.service
        applyNightModePreference()
        setUpToolbar()
        checkLocationUserEnabled()
        startAnimations()
        setListenersOfView()
    }

    private fun applyNightModePreference() {
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(this)
        val isNightMode =
            preferences.getBoolean(UserPrefs.IS_DARK_PREFERENCE, this.isNightModeSystemDefault())
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            delegate.applyDayNight()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            delegate.applyDayNight()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.mainToolbar)
        binding.mainToolbar.setOnMenuItemClickListener { itemMenu ->
            when (itemMenu.itemId) {
                R.id.item_settings -> {
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    true
                }
                R.id.item_refresh -> {
                    requestLocationData()
                    true
                }
                else -> false
            }
        }
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
            Toast.LENGTH_SHORT
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
                p1: PermissionToken?
            ) {
                showRationalDialogForPermissionsSettings(
                    getString(R.string.rationale_dialog_message)
                )
            }
        }

        Dexter
            .withContext(applicationContext)
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(locationPermissionsListener)
            .onSameThread()
            .check()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            100000
        ).build()

        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.getMainLooper()
        )
    }

    private fun startAnimations() {
        binding.llMainScreen.startAnimationFromId(applicationContext)
    }

    private fun setListenersOfView() {
    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {
        if (NetworkConnection.isNetworkAvailable(this)) {
            showCustomDialog()
            lifecycleScope.launch(Dispatchers.IO) {
                val response =
                    service.getWeather(
                        latitude,
                        longitude,
                        NetworkConnection.METRIC_UNIT,
                        openWeatherKey
                    )
                response.body()?.let { safeResponse ->
                    Timber.e("Message", response.message())
                    Timber.e("Weather Response", safeResponse.toString())
                    runOnUiThread {
                        setDetailsInView(safeResponse)
                    }
                }
                hideCustomDialog()
            }
        } else {
            showToastWithText(getString(R.string.message_no_internet_connection))
        }
    }

    private fun setDetailsInView(safeResponse: WeatherResponse) {
        safeResponse.weather[0].apply {
            binding.cvWeather.setTextMain(main)
            binding.cvWeather.setTextMainDescription(description)
        }
        safeResponse.main.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val unit = application.resources.configuration.locales
                binding.cvHumidity.setTextMain(
                    getString(R.string.text_template_actual_temp, temp, getUnit(unit.toString()))
                )
                binding.cvHumidity.setTextMainDescription(
                    getString(R.string.text_template_humidity, humidity)
                )
            }
            binding.cvMinMax.setTextMain(getString(R.string.text_template_temp_min, tempMin))
            binding.cvMinMax.setTextMainDescription(
                getString(
                    R.string.text_template_temp_max,
                    tempMax
                )
            )
        }
        with(safeResponse.sys) {
            val sunrise: Int = this.sunrise
            val sunset: Int = this.sunset
            binding.layoutSunsetSunrise.apply {
                tvSunriseTime.text = sunrise.toLong().unixTimeFormat(DatePattern.TIME_AM_PM.value)
                tvSunsetTime.text = sunset.toLong().unixTimeFormat(DatePattern.TIME_AM_PM.value)
            }
        }

        binding.cvSpeedWind.setTextMain(safeResponse.wind.speed.toString())
        binding.layoutCountry.tvName.text = safeResponse.name
        binding.layoutCountry.tvCountry.text = safeResponse.sys.country

        loadImageWeather(safeResponse)
    }

    private fun getUnit(unit: String): String {
        var value = "ºC"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "ºF"
        }
        return value
    }

    private fun loadImageWeather(safeResponse: WeatherResponse) {
        val requestManagerGlide = Glide
            .with(applicationContext)

        requestManagerGlide
            .load(
                "${UrlFactory.ICON_OPEN_WEATHER_URL.value}${safeResponse.weather[0].icon}.png"
            )
            .placeholder(R.drawable.loading_placeholder_image)
            .into(binding.cvWeather.getImageWeather())
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
                getString(R.string.rationale_dialog_negative_button_text)
            ) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showCustomDialog() {
        mProgressDialog = Dialog(this@MainActivity)
        mProgressDialog.apply {
            setContentView(R.layout.dialog_custom_progress)
            show()
        }
    }

    private fun hideCustomDialog() {
        mProgressDialog.dismiss()
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            mLastLocation?.let { safeLocation ->
                val latitude = safeLocation.latitude
                Timber.i(TAG_CURRENT_LATITUDE, "$latitude")
                val longitude = safeLocation.longitude
                Timber.i(TAG_CURRENT_LONGITUDE, "$longitude")
                getLocationWeatherDetails(latitude, longitude)
            }
        }
    }

    companion object {
        const val TAG_CURRENT_LATITUDE = "Current Latitude"
        const val TAG_CURRENT_LONGITUDE = "Current Longitude"
    }
}
