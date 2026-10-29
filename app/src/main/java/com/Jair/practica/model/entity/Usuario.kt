package com.Jair.practica.model.entity

data class Usuario(
    val id: String,
    val username: String,
    val nombre: String,
    val role: LoginRole,
    val habilitado: Boolean = true
)
