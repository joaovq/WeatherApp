package br.com.joaovitorqueiroz.weatherapp.models

import java.io.Serializable

data class Sys(
    val id: Int,
    val type: Int,
    val message: Double,
    val country: String,
    val sunrise: Int,
    val sunset: Int,
) : Serializable
