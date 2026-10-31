package com.Jair.practica.model.entity

data class ReportePerdida(
    val id: String,
    val personaAtendidaId: String,
    val descripcion: String,
    val lugarAproximado: String? = null,
    val fechaReporte: String,
    val estado: EstadoReportePerdida = EstadoReportePerdida.BUSCANDO,
    val creadoPor: String,
    val contacto: String? = null
)
