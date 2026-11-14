package com.Jair.practica.model.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.Query

data class UsuarioOperativoRemoto(
    val id: String,
    val nombre_completo: String,
    val nombre_usuario: String?,
    val correo: String?,
    val rol: String,
    val activo: Boolean
)

interface SupabaseUsuariosApi {
    @GET("usuarios")
    fun consultar(@Query("select") columnas: String = "id,nombre_completo,nombre_usuario,correo,rol,activo",
                  @Query("rol") rol: String = "eq.PERSONAL_REGISTRADOR"): Call<List<UsuarioOperativoRemoto>>

    @GET("usuarios")
    fun buscarPorId(@Query("id") id: String,
                    @Query("select") columnas: String = "id,nombre_completo,nombre_usuario,correo,rol,activo"): Call<List<UsuarioOperativoRemoto>>

    @Headers("Prefer: return=representation")
    @PATCH("usuarios")
    fun cambiarEstado(@Query("id") id: String,
                      @Body cambios: Map<String, Boolean>): Call<List<UsuarioOperativoRemoto>>
}
