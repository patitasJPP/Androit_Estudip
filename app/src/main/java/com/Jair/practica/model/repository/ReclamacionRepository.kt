package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.PreguntaVerificacion
import com.Jair.practica.model.entity.Reclamacion

enum class MotivoRechazo {
    ROL_NO_AUTORIZADO,
    RECLAMACION_NO_ENCONTRADA,
    RECLAMACION_CERRADA,
    OBJETO_ENTREGADO,
    DECISION_INVALIDA,
    MOTIVO_INVALIDO,
    PREGUNTA_INVALIDA
}

sealed interface ResultadoDecision {
    data class Registrada(
        val reclamacion: Reclamacion,
        val estadoAnterior: EstadoReclamacion?
    ) : ResultadoDecision

    data class Denegada(val motivo: MotivoRechazo) : ResultadoDecision
}

interface ReclamacionRepository {
    fun findById(id: String): Reclamacion?

    fun findByIdForActor(id: String, actorRol: LoginRole): Reclamacion?

    fun listByObject(objetoId: String): List<Reclamacion>

    fun listPendientes(actorRol: LoginRole): List<Reclamacion>

    fun save(reclamacion: Reclamacion): Reclamacion

    fun update(reclamacion: Reclamacion): Reclamacion

    fun registrarDecision(
        reclamacionId: String,
        nuevoEstado: EstadoReclamacion,
        motivo: String,
        preguntas: List<PreguntaVerificacion>,
        actorId: String,
        actorRol: LoginRole,
        fecha: String,
        idOperacion: String
    ): ResultadoDecision
}
