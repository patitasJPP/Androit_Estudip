package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.Evidencia
import com.Jair.practica.model.entity.LoginRole

interface EvidenciaRepository {
    fun findById(id: String): Evidencia?

    fun listByReclamacion(reclamacionId: String): List<Evidencia>

    fun save(evidencia: Evidencia): Evidencia

    fun listByReclamacion(
        reclamacionId: String,
        actorRol: LoginRole
    ): List<Evidencia> = if (actorRol == LoginRole.ADMINISTRADOR_SAE) {
        listByReclamacion(reclamacionId)
    } else {
        emptyList()
    }
}
