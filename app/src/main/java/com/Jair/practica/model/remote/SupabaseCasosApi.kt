package com.Jair.practica.model.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Query

data class ReporteRemoto(
    val id: String? = null,
    val codigo_reporte: String,
    val persona_atendida: String,
    val contacto: String,
    val descripcion: String,
    val lugar_aproximado: String? = null,
    val estado: String = "BUSCANDO",
    val registrado_por: String
)

data class ReclamacionRemota(
    val id: String? = null,
    val codigo_reclamacion: String,
    val reporte_perdida_id: String,
    val objeto_encontrado_id: String,
    val solicitante: String,
    val evidencia_aportada: String? = null,
    val preguntas_verificacion: String? = null,
    val decision: String = "PENDIENTE_VERIFICACION",
    val motivo_decision: String? = null,
    val revisada_por: String? = null
)

data class ConfirmarEntregaRemota(
    val p_codigo_reclamacion: String,
    val p_codigo_entrega: String,
    val p_receptor: String,
    val p_dni_receptor: String,
    val p_correo_receptor: String,
    val p_firma_url: String
)

data class EntregaRemota(
    val id: String,
    val codigo_entrega: String,
    val reclamacion_id: String,
    val objeto_encontrado_id: String,
    val receptor: String,
    val dni_receptor: String?,
    val correo_receptor: String?,
    val firma_url: String?,
    val entregado_en: String?,
    val estado: String
)

interface SupabaseCasosApi {
    @GET("reportes_perdida")
    fun consultarReportes(@Query("select") columnas: String = "*"): Call<List<ReporteRemoto>>

    @GET("reclamaciones")
    fun consultarReclamaciones(@Query("select") columnas: String = "*"): Call<List<ReclamacionRemota>>

    @GET("entregas")
    fun consultarEntregas(@Query("select") columnas: String = "*"): Call<List<EntregaRemota>>

    @GET("reportes_perdida")
    fun buscarReporte(
        @Query("codigo_reporte") codigo: String,
        @Query("select") columnas: String = "*"
    ): Call<List<ReporteRemoto>>

    @Headers("Prefer: return=representation")
    @POST("reportes_perdida")
    fun crearReporte(@Body reporte: ReporteRemoto): Call<List<ReporteRemoto>>

    @GET("reclamaciones")
    fun buscarReclamacion(
        @Query("codigo_reclamacion") codigo: String,
        @Query("select") columnas: String = "*"
    ): Call<List<ReclamacionRemota>>

    @Headers("Prefer: return=representation")
    @POST("reclamaciones")
    fun crearReclamacion(@Body reclamacion: ReclamacionRemota): Call<List<ReclamacionRemota>>

    @Headers("Prefer: return=representation")
    @PATCH("reclamaciones")
    fun actualizarReclamacion(
        @Query("id") id: String,
        @Body cambios: Map<String, String?>
    ): Call<List<ReclamacionRemota>>

    @POST("rpc/confirmar_entrega_segura")
    fun confirmarEntrega(@Body solicitud: ConfirmarEntregaRemota): Call<String>
}
