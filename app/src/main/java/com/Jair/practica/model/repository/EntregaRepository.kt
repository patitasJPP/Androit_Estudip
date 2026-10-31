package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.EstadoEntrega
import com.Jair.practica.model.entity.Entrega

enum class MotivoRechazoEntrega {
    ROL_NO_AUTORIZADO,
    ENTREGA_NO_ENCONTRADA,
    RECLAMACION_NO_APROBADA,
    OBJETO_ENTREGADO,
    FALTA_FIRMA,
    DATOS_INVALIDOS,
    ESTADO_INVALIDO
}

sealed interface ResultadoEntrega {
    data class Registrada(
        val entrega: Entrega,
        val estadoAnterior: EstadoEntrega?
    ) : ResultadoEntrega

    data class Denegada(val motivo: MotivoRechazoEntrega) : ResultadoEntrega
}

interface EntregaRepository {
    fun findById(id: String): Entrega?

    fun save(entrega: Entrega): Entrega

    fun update(entrega: Entrega): Entrega

    fun list(): List<Entrega>

    fun confirmarEntrega(
        reclamacionId: String,
        receptorId: String,
        administradorId: String,
        firmaCapturada: Boolean,
        fecha: String,
        idOperacion: String
    ): ResultadoEntrega
}
