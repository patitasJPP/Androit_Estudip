package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.remote.LoginSupabaseRequest
import com.Jair.practica.model.remote.SupabaseAuthClient
import com.Jair.practica.model.remote.SupabaseClient
import com.Jair.practica.model.remote.SesionRemota
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.Callable
import retrofit2.Response

/** Autenticación real y autorización contra la tabla de usuarios de Supabase. */
class SupabaseAuthRepository : AuthRepository {
    @Volatile var ultimoError: String = "Correo o contraseña incorrectos. Comprueba tus credenciales."

    override fun authenticate(role: LoginRole, username: String, password: String): Boolean {
        val email = username.trim()
        SesionRemota.cerrar()
        ultimoError = ""
        val pool = Executors.newSingleThreadExecutor()
        val respuesta: Response<com.Jair.practica.model.remote.SesionSupabase> = try {
            val tarea: Future<Response<com.Jair.practica.model.remote.SesionSupabase>> = pool.submit(Callable {
                SupabaseAuthClient.api.iniciarSesion(
                    request = LoginSupabaseRequest(email, password)
                ).execute()
            })
            tarea.get(8, TimeUnit.SECONDS)
        } catch (error: Exception) {
            ultimoError = "No se pudo conectar. Comprueba tu conexión a internet e inténtalo de nuevo."
            pool.shutdownNow()
            return false
        }
        pool.shutdownNow()
        if (!respuesta.isSuccessful) {
            ultimoError = when (respuesta.code()) {
                400, 401, 422 -> "Correo o contraseña incorrectos. Comprueba tus credenciales."
                429 -> "Demasiados intentos. Espera un momento e inténtalo de nuevo."
                else -> "No se pudo iniciar sesión. Inténtalo de nuevo más tarde."
            }
            return false
        }
        val token = respuesta.body()?.access_token
        val usuarioId = respuesta.body()?.user?.id
        val correoAutenticado = respuesta.body()?.user?.email.orEmpty()
        if (token.isNullOrBlank() || usuarioId.isNullOrBlank()) {
            ultimoError = "Supabase no devolvió una sesión válida"
            return false
        }
        SesionRemota.accessToken = token
        SesionRemota.usuarioId = usuarioId
        SesionRemota.correo = correoAutenticado
        val perfil = try {
            val consulta = Executors.newSingleThreadExecutor()
            try {
                val resultado = consulta.submit(Callable {
                    SupabaseClient.usuarios.buscarPorId("eq.$usuarioId").execute()
                }).get(8, TimeUnit.SECONDS)
                if (!resultado.isSuccessful) {
                    ultimoError = "No se pudo verificar el acceso: HTTP ${resultado.code()}"
                    null
                } else resultado.body()?.singleOrNull()
            } finally { consulta.shutdownNow() }
        } catch (error: Exception) {
            ultimoError = "No se pudo verificar el rol: ${error.message ?: "error de red"}"
            null
        }
        val rolEsperado = if (role == LoginRole.ADMINISTRADOR_SAE) "ADMINISTRADOR_SAE" else "PERSONAL_REGISTRADOR"
        if (perfil == null || !perfil.activo || perfil.rol != rolEsperado ||
            !perfil.correo.equals(correoAutenticado, ignoreCase = true)) {
            if (ultimoError.isBlank()) ultimoError = "La cuenta está deshabilitada o no corresponde al rol seleccionado"
            SesionRemota.cerrar()
            return false
        }
        SesionRemota.nombre = perfil.nombre_completo
        SesionRemota.rol = perfil.rol
        ultimoError = ""
        return true
    }
}
