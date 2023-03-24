package br.com.joaovitorqueiroz.weatherapp.core.network.factory

import br.com.joaovitorqueiroz.weatherapp.BuildConfig

val openWeatherKey = BuildConfig.OPEN_WEATHER

enum class UrlFactory(val value: String) {
    OPEN_WEATHER_URL("https://api.openweathermap.org/data/"),
    ICON_OPEN_WEATHER_URL("https://openweathermap.org/img/wn/")
}
