package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.Aviso

interface AvisoRepository {
    fun listByRecipient(usuarioId: String): List<Aviso>
    fun save(aviso: Aviso): Aviso
    fun update(aviso: Aviso): Aviso
}
