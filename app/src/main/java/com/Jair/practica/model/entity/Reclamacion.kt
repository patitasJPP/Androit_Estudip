package com.Jair.practica.model.entity

data class Reclamacion(
    val id: String,
    val objetoEncontradoId: String,
    val personaAtendidaId: String,
    val reportePerdidaId: String? = null,
    val evidenciaIds: List<String> = emptyList(),
    val estado: EstadoReclamacion = EstadoReclamacion.PENDIENTE_VERIFICACION,
    val motivoDecision: String? = null,
    val revisadoPor: String? = null
)
