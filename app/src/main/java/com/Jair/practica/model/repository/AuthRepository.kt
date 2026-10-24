package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.LoginRole

interface AuthRepository {
    fun authenticate(role: LoginRole, username: String, password: String): Boolean
}
