package com.Jair.practica.model.local

import com.Jair.practica.model.entity.Evidencia
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.repository.EvidenciaRepository

class LocalEvidenciaRepository(
    private val store: LocalDataStore = AlmacenLocal.instancia
) : EvidenciaRepository {

    override fun findById(id: String): Evidencia? =
        store.evidencias.firstOrNull { it.id == id }

    override fun listByReclamacion(reclamacionId: String): List<Evidencia> =
        store.evidencias.filter { it.reclamacionId == reclamacionId }

    override fun listByReclamacion(
        reclamacionId: String,
        actorRol: LoginRole
    ): List<Evidencia> = if (actorRol == LoginRole.ADMINISTRADOR_SAE) {
        listByReclamacion(reclamacionId)
    } else {
        store.evidencias.filter { it.reclamacionId == reclamacionId && !it.reservada }
    }

    override fun save(evidencia: Evidencia): Evidencia {
        store.evidencias.add(evidencia)
        return evidencia
    }
}
