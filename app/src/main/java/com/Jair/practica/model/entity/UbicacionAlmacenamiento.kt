package com.Jair.practica.model.entity

data class UbicacionAlmacenamiento(
    val id: String,
    val tipo: TipoUbicacion,
    val codigo: String,
    val descripcion: String? = null,
    val activa: Boolean = true
)
