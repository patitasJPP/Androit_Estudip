package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.EventoAuditoria

interface AuditoriaRepository {
    fun save(evento: EventoAuditoria): EventoAuditoria
    fun listByEntity(entidadTipo: String, entidadId: String): List<EventoAuditoria>
}
