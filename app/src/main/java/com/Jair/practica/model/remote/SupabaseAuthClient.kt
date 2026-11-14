package com.Jair.practica.model.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseAuthClient {
    val api: SupabaseAuthApi by lazy {
        val http = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain ->
            chain.proceed(chain.request().newBuilder()
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .build())
        }).build()
        Retrofit.Builder().baseUrl(SupabaseConfig.AUTH_URL).client(http)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(SupabaseAuthApi::class.java)
    }
}
