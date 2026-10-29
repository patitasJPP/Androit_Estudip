package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.Entrega

interface EntregaRepository {
    fun findById(id: String): Entrega?
    fun save(entrega: Entrega): Entrega
    fun update(entrega: Entrega): Entrega
    fun list(): List<Entrega>
}
