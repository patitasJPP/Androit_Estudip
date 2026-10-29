package com.Jair.practica.model.entity

data class PersonaAtendida(
    val id: String,
    val nombre: String,
    val vinculoUniversidad: String,
    val identificador: String? = null,
    val correo: String? = null,
    val telefono: String? = null
)
