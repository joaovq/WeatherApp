package br.com.joaovitorqueiroz.weatherapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkConnection {

    const val METRIC_UNIT = "metric"
    fun isNetworkAvailable(context: Context): Boolean {
        val result: Boolean
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activityNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            when {
                activityNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activityNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activityNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnectedOrConnecting
        }
        return result
    }
}
