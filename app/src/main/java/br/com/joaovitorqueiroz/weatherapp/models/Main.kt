package br.com.joaovitorqueiroz.weatherapp.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Main(
    val temp: Double,
    val pressure: Double,
    val humidity: Int,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("sea_level") val seaLevel: Double,
    @SerializedName("grnd_level") val grndLevel: Double,
) : Serializable
