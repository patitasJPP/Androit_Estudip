package com.Jair.practica.model.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Query

data class ObjetoEncontradoRemoto(
    val id: String? = null,
    val codigo_custodia: String? = null,
    val categoria: String,
    val descripcion: String,
    val lugar_hallazgo: String? = null,
    val fecha_hallazgo: String? = null,
    val estado: String = "HALLAZGO_REPORTADO",
    val registrado_por: String
)

interface SupabaseApi {
    @Headers("Prefer: return=representation")
    @GET("objetos_encontrados")
    fun consultarObjetos(@Query("select") columnas: String = "*"): Call<List<ObjetoEncontradoRemoto>>

    @Headers("Prefer: return=representation")
    @POST("objetos_encontrados")
    fun registrarObjeto(@Body objeto: ObjetoEncontradoRemoto): Call<List<ObjetoEncontradoRemoto>>

    @Headers("Prefer: return=representation")
    @PATCH("objetos_encontrados")
    fun actualizarObjeto(
        @Query("id") id: String,
        @Body objeto: ObjetoEncontradoRemoto
    ): Call<List<ObjetoEncontradoRemoto>>

    @GET("objetos_encontrados")
    fun buscarObjetoPorCodigo(
        @Query("codigo_custodia") codigo: String,
        @Query("select") columnas: String = "*"
    ): Call<List<ObjetoEncontradoRemoto>>

    @GET("objetos_encontrados")
    fun buscarObjetoPorId(
        @Query("id") id: String,
        @Query("select") columnas: String = "*"
    ): Call<List<ObjetoEncontradoRemoto>>
}
