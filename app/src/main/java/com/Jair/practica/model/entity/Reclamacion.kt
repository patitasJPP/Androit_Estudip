package com.Jair.practica.model.entity

data class Reclamacion(
    val id: String,
    val objetoEncontradoId: String,
    val personaAtendidaId: String,
    val reportePerdidaId: String? = null,
    val evidenciaIds: List<String> = emptyList(),
    val estado: EstadoReclamacion = EstadoReclamacion.PENDIENTE_VERIFICACION,
    val motivoDecision: String? = null,
    val revisadoPor: String? = null,
    val fechaRegistro: String? = null,
    val descripcionReclamante: String? = null,
    val caracteristicasDeclaradas: String? = null,
    val preguntas: List<PreguntaVerificacion> = emptyList(),
    val fechaDecision: String? = null,
    val estadoAnterior: EstadoReclamacion? = null,
    val idOperacion: String? = null
)

data class PreguntaVerificacion(
    val id: String,
    val pregunta: String,
    val respuesta: String? = null
)
