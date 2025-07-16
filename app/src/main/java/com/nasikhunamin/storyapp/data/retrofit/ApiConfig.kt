package com.nasikhunamin.storyapp.data.retrofit

import com.nasikhunamin.storyapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    var base_url = "YOUR_API_KEY"
    fun getApiService(): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().setLevel(
            if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        )
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(base_url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(ApiService::class.java)
    }
}