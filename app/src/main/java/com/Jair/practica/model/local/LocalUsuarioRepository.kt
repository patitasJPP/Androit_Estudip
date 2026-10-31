package com.Jair.practica.model.local

import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.Usuario
import com.Jair.practica.model.repository.UsuarioRepository

class LocalUsuarioRepository(
    private val store: LocalDataStore = AlmacenLocal.instancia
) : UsuarioRepository {
    override fun findById(id: String): Usuario? =
        store.usuarios.firstOrNull { it.id == id }

    override fun findByUsername(username: String): Usuario? =
        store.usuarios.firstOrNull { it.username.equals(username, ignoreCase = true) }

    override fun save(usuario: Usuario): Usuario {
        val index = store.usuarios.indexOfFirst { it.id == usuario.id }
        if (index >= 0) {
            store.usuarios[index] = usuario
        } else {
            store.usuarios.add(usuario)
        }
        return usuario
    }

    override fun setEnabled(id: String, enabled: Boolean): Usuario? {
        val index = store.usuarios.indexOfFirst { it.id == id }
        if (index >= 0) {
            val usuarioActualizado = store.usuarios[index].copy(habilitado = enabled)
            store.usuarios[index] = usuarioActualizado
            return usuarioActualizado
        }
        return null
    }
}
