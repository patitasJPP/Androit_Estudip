package com.Jair.practica.model.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer ${SesionRemota.accessToken ?: SupabaseConfig.ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }).build()

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.BASE_URL)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }

    val casos: SupabaseCasosApi by lazy {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.BASE_URL)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseCasosApi::class.java)
    }

    val usuarios: SupabaseUsuariosApi by lazy {
        Retrofit.Builder().baseUrl(SupabaseConfig.BASE_URL).client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(SupabaseUsuariosApi::class.java)
    }
}
