package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.Evidencia

interface EvidenciaRepository {
    fun findById(id: String): Evidencia?
    fun listByReclamacion(reclamacionId: String): List<Evidencia>
    fun save(evidencia: Evidencia): Evidencia
}
