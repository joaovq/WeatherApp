package br.com.joaovitorqueiroz.weatherapp.util.extension

import java.text.SimpleDateFormat
import java.util.*

fun Long.unixTimeFormat(pattern: String): String {
    val date = Date(this * 1000L)
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    simpleDateFormat.timeZone = TimeZone.getDefault()
    return simpleDateFormat.format(date)
}
