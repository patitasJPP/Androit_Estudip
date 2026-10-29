package com.Jair.practica.model.entity

data class Evidencia(
    val id: String,
    val objetoEncontradoId: String? = null,
    val reclamacionId: String? = null,
    val tipo: String,
    val descripcion: String,
    val referenciaArchivo: String? = null,
    val reservada: Boolean = true,
    val registradaPor: String
)
