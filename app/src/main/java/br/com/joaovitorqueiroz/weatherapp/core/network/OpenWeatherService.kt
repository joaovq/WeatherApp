package br.com.joaovitorqueiroz.weatherapp.core.network

import br.com.joaovitorqueiroz.weatherapp.core.network.factory.UrlFactory
import br.com.joaovitorqueiroz.weatherapp.models.WeatherResponse
import br.com.joaovitorqueiroz.weatherapp.util.config.RetrofitConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("appid") appid: String?,
    ): Response<WeatherResponse>
}

object OpenWeatherService {
    val service by lazy {
        RetrofitConfig
            .createInstanceRetrofitBuilder(UrlFactory.OPEN_WEATHER_URL.value)
            .create(WeatherService::class.java)
    }
}
