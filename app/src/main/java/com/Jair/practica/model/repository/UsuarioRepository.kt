package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.Usuario

interface UsuarioRepository {
    fun findById(id: String): Usuario?
    fun findByUsername(username: String): Usuario?
    fun save(usuario: Usuario): Usuario
    fun setEnabled(id: String, enabled: Boolean): Usuario?
}
