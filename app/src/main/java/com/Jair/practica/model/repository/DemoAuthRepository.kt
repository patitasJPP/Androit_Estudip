package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.LoginRole

/** Mantiene el comportamiento actual mientras no exista un servidor de autenticación. */
class DemoAuthRepository : AuthRepository {
    override fun authenticate(role: LoginRole, username: String, password: String): Boolean {
        val expectedPassword = when (role) {
            LoginRole.PERSONAL_REGISTRADOR -> "jair"
            LoginRole.ADMINISTRADOR_SAE -> "jair123"
        }
        return username.trim() == "jair" && password == expectedPassword
    }
}
