package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.Reclamacion

interface ReclamacionRepository {
    fun findById(id: String): Reclamacion?
    fun listByObject(objetoId: String): List<Reclamacion>
    fun save(reclamacion: Reclamacion): Reclamacion
    fun update(reclamacion: Reclamacion): Reclamacion
}
