package com.Jair.practica.model.entity

data class EventoAuditoria(
    val id: String,
    val entidadTipo: String,
    val entidadId: String,
    val accion: String,
    val usuarioId: String,
    val fecha: String,
    val detalle: String? = null,
    val estadoAnterior: String? = null,
    val estadoNuevo: String? = null
)
