package com.Jair.practica.model.remote

/** Sesión Supabase activa para adjuntar JWT a las peticiones REST. */
object SesionRemota {
    @Volatile var accessToken: String? = null
    @Volatile var usuarioId: String? = null
    @Volatile var correo: String? = null
    @Volatile var nombre: String? = null
    @Volatile var rol: String? = null

    fun cerrar() {
        accessToken = null
        usuarioId = null
        correo = null
        nombre = null
        rol = null
    }
}
