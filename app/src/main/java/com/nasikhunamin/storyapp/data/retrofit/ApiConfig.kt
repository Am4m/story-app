package com.nasikhunamin.storyapp.data.retrofit

import com.nasikhunamin.storyapp.BuildConfig
import com.nasikhunamin.storyapp.utils.EspressoIdlingResource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {

    var base_url = "http://127.0.0.1:8080/"
    fun getApiService(): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().setLevel(
            if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        )
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                EspressoIdlingResource.increment()
                val request = chain.request()
                try {
                    chain.proceed(request)
                } finally {
                    EspressoIdlingResource.decrement()
                }
            }
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .baseUrl(base_url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(ApiService::class.java)
    }
}