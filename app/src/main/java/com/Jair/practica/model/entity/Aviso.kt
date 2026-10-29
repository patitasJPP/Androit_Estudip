package com.Jair.practica.model.entity

data class Aviso(
    val id: String,
    val destinatarioId: String,
    val titulo: String,
    val mensaje: String,
    val creadoEn: String,
    val estado: EstadoAviso = EstadoAviso.PENDIENTE
)
