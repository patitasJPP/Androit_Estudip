package com.Jair.practica.model.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class LoginSupabaseRequest(val email: String, val password: String)
data class SesionSupabase(val access_token: String?, val user: UsuarioSupabase?)
data class UsuarioSupabase(val id: String?, val email: String?)

interface SupabaseAuthApi {
    @POST("token")
    fun iniciarSesion(
        @Query("grant_type") grantType: String = "password",
        @Body request: LoginSupabaseRequest
    ): Call<SesionSupabase>

    @POST("recover")
    fun recuperarClave(@Body solicitud: Map<String, String>): Call<okhttp3.ResponseBody>
}
