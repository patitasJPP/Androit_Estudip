package com.Jair.practica.model.local

import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.EventoAuditoria
import com.Jair.practica.model.entity.PreguntaVerificacion
import com.Jair.practica.model.entity.Reclamacion
import com.Jair.practica.model.repository.MotivoRechazo
import com.Jair.practica.model.repository.ReclamacionRepository
import com.Jair.practica.model.repository.ResultadoDecision
import com.Jair.practica.model.entity.LoginRole

class LocalReclamacionRepository(
    private val store: LocalDataStore = AlmacenLocal.instancia
) : ReclamacionRepository {

    override fun findById(id: String): Reclamacion? =
        store.reclamaciones.firstOrNull { it.id == id }

    override fun findByIdForActor(id: String, actorRol: LoginRole): Reclamacion? =
        if (actorRol == LoginRole.ADMINISTRADOR_SAE) findById(id) else null

    override fun listByObject(objetoId: String): List<Reclamacion> =
        store.reclamaciones.filter { it.objetoEncontradoId == objetoId }

    override fun listPendientes(actorRol: LoginRole): List<Reclamacion> =
        if (actorRol == LoginRole.ADMINISTRADOR_SAE) {
            store.reclamaciones.filter { it.estado == EstadoReclamacion.PENDIENTE_VERIFICACION }
        } else {
            emptyList()
        }

    override fun save(reclamacion: Reclamacion): Reclamacion {
        val index = store.reclamaciones.indexOfFirst { it.id == reclamacion.id }
        if (index >= 0) {
            store.reclamaciones[index] = reclamacion
        } else {
            store.reclamaciones.add(reclamacion)
        }
        return reclamacion
    }

    override fun update(reclamacion: Reclamacion): Reclamacion {
        val index = store.reclamaciones.indexOfFirst { it.id == reclamacion.id }
        if (index >= 0) {
            store.reclamaciones[index] = reclamacion
        }
        return reclamacion
    }

    override fun registrarDecision(
        reclamacionId: String,
        nuevoEstado: EstadoReclamacion,
        motivo: String,
        preguntas: List<PreguntaVerificacion>,
        actorId: String,
        actorRol: LoginRole,
        fecha: String,
        idOperacion: String
    ): ResultadoDecision {
        if (actorRol != LoginRole.ADMINISTRADOR_SAE) {
            return ResultadoDecision.Denegada(MotivoRechazo.ROL_NO_AUTORIZADO)
        }

        val actual = store.reclamaciones.firstOrNull { it.id == reclamacionId }
            ?: return ResultadoDecision.Denegada(MotivoRechazo.RECLAMACION_NO_ENCONTRADA)

        if (idOperacion.isNotBlank() && store.operacionesAplicadas.contains(idOperacion)) {
            return ResultadoDecision.Registrada(actual, actual.estado)
        }

        if (actual.estado == EstadoReclamacion.APROBADA ||
            actual.estado == EstadoReclamacion.RECHAZADA ||
            actual.estado == EstadoReclamacion.ATENDIDA
        ) {
            return ResultadoDecision.Denegada(MotivoRechazo.RECLAMACION_CERRADA)
        }

        val objeto = store.objetos.firstOrNull { it.id == actual.objetoEncontradoId }
        if (objeto?.estado == EstadoObjeto.ENTREGADO) {
            return ResultadoDecision.Denegada(MotivoRechazo.OBJETO_ENTREGADO)
        }

        if (nuevoEstado != EstadoReclamacion.PENDIENTE_VERIFICACION &&
            nuevoEstado != EstadoReclamacion.APROBADA &&
            nuevoEstado != EstadoReclamacion.RECHAZADA
        ) {
            return ResultadoDecision.Denegada(MotivoRechazo.DECISION_INVALIDA)
        }

        val motivoLimpio = motivo.trim()
        if (motivoLimpio.length < 10 || motivoLimpio.length > 300) {
            return ResultadoDecision.Denegada(MotivoRechazo.MOTIVO_INVALIDO)
        }

        val preguntasValidadas = preguntas.map { it.pregunta.trim() }
        if (preguntasValidadas.any { it.length < 10 || it.length > 200 }) {
            return ResultadoDecision.Denegada(MotivoRechazo.PREGUNTA_INVALIDA)
        }

        val estadoAnterior = actual.estado
        val actualizada = actual.copy(
            estado = nuevoEstado,
            motivoDecision = motivoLimpio,
            revisadoPor = actorId,
            fechaDecision = fecha,
            estadoAnterior = estadoAnterior,
            preguntas = preguntas.mapIndexed { index, pregunta ->
                PreguntaVerificacion(
                    id = "${reclamacionId}-p${index}",
                    pregunta = pregunta.pregunta.trim(),
                    respuesta = pregunta.respuesta?.takeIf { it.isNotBlank() }
                )
            }
        )

        update(actualizada)
        store.operacionesAplicadas.add(idOperacion)
        store.eventosAuditoria.add(
            EventoAuditoria(
                id = "aud-${store.eventosAuditoria.size + 1}",
                entidadTipo = "Reclamacion",
                entidadId = reclamacionId,
                accion = "DECISION_VERIFICACION",
                usuarioId = actorId,
                fecha = fecha,
                detalle = "Decision ${nuevoEstado.name}. Motivo: $motivoLimpio",
                estadoAnterior = estadoAnterior.name,
                estadoNuevo = nuevoEstado.name
            )
        )

        return ResultadoDecision.Registrada(actualizada, estadoAnterior)
    }
}
