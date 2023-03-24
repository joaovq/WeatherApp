package br.com.joaovitorqueiroz.weatherapp.util.config

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitConfig {
    companion object {
        private var instance: Retrofit? = null

        fun createInstanceRetrofitBuilder(baseUrl: String): Retrofit {
            if (instance == null) {
                instance = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return instance as Retrofit
        }
    }
}
