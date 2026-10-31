package com.Jair.practica.model.entity

data class ReportePerdida(
    val id: String,
    val personaAtendidaId: String? = null,
    val descripcion: String,
    val lugarAproximado: String? = null,
    val fechaReporte: String,
    val estado: EstadoReportePerdida = EstadoReportePerdida.SIN_COINCIDENCIA,
    val creadoPor: String,
    // --- Integrante 3
    val contactoNombre: String? = null,
    val contactoCorreo: String? = null,
    val contactoTelefono: String? = null,
    val categoria: String? = null,
    val color: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val pabellon: String? = null,
    val piso: String? = null,
    val aula: String? = null,
    val coincidenciaIds: List<String> = emptyList(),
    val objetoSeguimientoId: String? = null,
    val idOperacion: String? = null
)
