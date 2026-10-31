package com.Jair.practica.model.local

import com.Jair.practica.model.entity.EventoAuditoria
import com.Jair.practica.model.repository.AuditoriaRepository

class LocalAuditoriaRepository(
    private val store: LocalDataStore = AlmacenLocal.instancia
) : AuditoriaRepository {

    override fun save(evento: EventoAuditoria): EventoAuditoria {
        store.eventosAuditoria.add(evento)
        return evento
    }

    override fun listByEntity(entidadTipo: String, entidadId: String): List<EventoAuditoria> =
        store.eventosAuditoria.filter {
            it.entidadTipo == entidadTipo && it.entidadId == entidadId
        }
}
