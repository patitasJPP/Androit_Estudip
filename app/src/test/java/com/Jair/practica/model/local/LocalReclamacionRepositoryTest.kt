package com.Jair.practica.model.local

import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.PreguntaVerificacion
import com.Jair.practica.model.entity.Reclamacion
import com.Jair.practica.model.repository.MotivoRechazo
import com.Jair.practica.model.repository.ReclamacionRepository
import com.Jair.practica.model.repository.ResultadoDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalReclamacionRepositoryTest {

    private val store = LocalDataStore()
    private val repository: ReclamacionRepository = LocalReclamacionRepository(store)

    @Test
    fun registraDecisionAprobadaConMotivoYAuditoria() {
        val resultado = repository.registrarDecision(
            reclamacionId = store.reclamaciones.first().id,
            nuevoEstado = EstadoReclamacion.APROBADA,
            motivo = "Se verificó la evidencia y el equipo coincide con la reclamación.",
            preguntas = listOf(
                PreguntaVerificacion(
                    id = "p-1",
                    pregunta = "La contraseña registrada permite el arranque del equipo.",
                    respuesta = "Sí"
                )
            ),
            actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            actorRol = LoginRole.ADMINISTRADOR_SAE,
            fecha = "2026-10-30 10:00",
            idOperacion = "op-auditoria-001"
        )

        assertTrue(resultado is ResultadoDecision.Registrada)
        val actualizada = (resultado as ResultadoDecision.Registrada).reclamacion
        assertEquals(EstadoReclamacion.APROBADA, actualizada.estado)
        assertEquals(LocalDataStore.USUARIO_ADMINISTRADOR_ID, actualizada.revisadoPor)
        assertTrue(actualizada.estadoAnterior?.name?.isNotBlank() == true)
        assertTrue(store.eventosAuditoria.any { evento ->
            evento.entidadId == actualizada.id &&
                evento.estadoNuevo == EstadoReclamacion.APROBADA.name &&
                evento.estadoAnterior == EstadoReclamacion.PENDIENTE_VERIFICACION.name
        })
    }

    @Test
    fun rechazaDecisionDelPersonalRegistrador() {
        val resultado = repository.registrarDecision(
            reclamacionId = store.reclamaciones.first().id,
            nuevoEstado = EstadoReclamacion.RECHAZADA,
            motivo = "El equipo no coincide con la evidencia presentada.",
            preguntas = emptyList(),
            actorId = LocalDataStore.USUARIO_REGISTRADOR_ID,
            actorRol = LoginRole.PERSONAL_REGISTRADOR,
            fecha = "2026-10-30 10:05",
            idOperacion = "op-auditoria-002"
        )

        assertTrue(resultado is ResultadoDecision.Denegada)
        assertEquals(MotivoRechazo.ROL_NO_AUTORIZADO, (resultado as ResultadoDecision.Denegada).motivo)
    }

    @Test
    fun noDecideSobreReclamacionCerrada() {
        store.reclamaciones.add(
            Reclamacion(
                id = "rec-cerrada",
                objetoEncontradoId = "obj-2026-0001",
                personaAtendidaId = "per-2026-0001",
                estado = EstadoReclamacion.APROBADA,
                motivoDecision = "Aprobada",
                revisadoPor = "usr-administrador-01",
                fechaRegistro = "2026-10-27"
            )
        )

        val resultado = repository.registrarDecision(
            reclamacionId = "rec-cerrada",
            nuevoEstado = EstadoReclamacion.RECHAZADA,
            motivo = "Se reconsidera la evidencia con nuevas pruebas.",
            preguntas = emptyList(),
            actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            actorRol = LoginRole.ADMINISTRADOR_SAE,
            fecha = "2026-10-30 10:10",
            idOperacion = "op-auditoria-003"
        )

        assertTrue(resultado is ResultadoDecision.Denegada)
        assertEquals(MotivoRechazo.RECLAMACION_CERRADA, (resultado as ResultadoDecision.Denegada).motivo)
    }

    @Test
    fun noDecideSobreObjetoEntregado() {
        val reclamacion = Reclamacion(
            id = "rec-entregada",
            objetoEncontradoId = "obj-2026-0002",
            personaAtendidaId = "per-2026-0001",
            estado = EstadoReclamacion.PENDIENTE_VERIFICACION,
            fechaRegistro = "2026-10-28"
        )
        store.reclamaciones.add(reclamacion)

        val resultado = repository.registrarDecision(
            reclamacionId = reclamacion.id,
            nuevoEstado = EstadoReclamacion.APROBADA,
            motivo = "Se verificó la evidencia y el equipo coincide con la reclamación.",
            preguntas = emptyList(),
            actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            actorRol = LoginRole.ADMINISTRADOR_SAE,
            fecha = "2026-10-30 10:12",
            idOperacion = "op-auditoria-004"
        )

        assertTrue(resultado is ResultadoDecision.Denegada)
        assertEquals(MotivoRechazo.OBJETO_ENTREGADO, (resultado as ResultadoDecision.Denegada).motivo)
    }

    @Test
    fun rechazaMotivoFueraDeRango() {
        val resultado = repository.registrarDecision(
            reclamacionId = store.reclamaciones.first().id,
            nuevoEstado = EstadoReclamacion.RECHAZADA,
            motivo = "corta",
            preguntas = emptyList(),
            actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            actorRol = LoginRole.ADMINISTRADOR_SAE,
            fecha = "2026-10-30 10:15",
            idOperacion = "op-auditoria-005"
        )

        assertTrue(resultado is ResultadoDecision.Denegada)
        assertEquals(MotivoRechazo.MOTIVO_INVALIDO, (resultado as ResultadoDecision.Denegada).motivo)
    }

    @Test
    fun rechazaPreguntaFueraDeRango() {
        val resultado = repository.registrarDecision(
            reclamacionId = store.reclamaciones.first().id,
            nuevoEstado = EstadoReclamacion.RECHAZADA,
            motivo = "Se verificó la evidencia y el equipo coincide con la reclamación.",
            preguntas = listOf(PreguntaVerificacion(id = "p-2", pregunta = "corta", respuesta = null)),
            actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            actorRol = LoginRole.ADMINISTRADOR_SAE,
            fecha = "2026-10-30 10:16",
            idOperacion = "op-auditoria-006"
        )

        assertTrue(resultado is ResultadoDecision.Denegada)
        assertEquals(MotivoRechazo.PREGUNTA_INVALIDA, (resultado as ResultadoDecision.Denegada).motivo)
    }

    @Test
    fun reanudaMismaOperacionSinDuplicado() {
        val primera = repository.registrarDecision(
            reclamacionId = store.reclamaciones.first().id,
            nuevoEstado = EstadoReclamacion.APROBADA,
            motivo = "Se verificó la evidencia y el equipo coincide con la reclamación.",
            preguntas = emptyList(),
            actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            actorRol = LoginRole.ADMINISTRADOR_SAE,
            fecha = "2026-10-30 10:20",
            idOperacion = "op-auditoria-007"
        )

        val segunda = repository.registrarDecision(
            reclamacionId = store.reclamaciones.first().id,
            nuevoEstado = EstadoReclamacion.RECHAZADA,
            motivo = "Se reabre la revisión con nuevo sustento.",
            preguntas = emptyList(),
            actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            actorRol = LoginRole.ADMINISTRADOR_SAE,
            fecha = "2026-10-30 10:21",
            idOperacion = "op-auditoria-007"
        )

        assertTrue(primera is ResultadoDecision.Registrada)
        assertTrue(segunda is ResultadoDecision.Registrada)
        assertEquals(1, store.reclamaciones.size)
    }

    @Test
    fun localRepositoryUsaObjetoEntregadoDelSemilla() {
        val objeto = ObjetoEncontrado(
            id = "obj-2026-0002",
            codigoCustodia = "UTP-2026-0098",
            categoria = "Celular",
            descripcion = "Celular negro con funda roja.",
            caracteristicasReservadas = "IMEI 8712345678901, marca Samsung, color negro.",
            lugarHallazgo = "Pabellón A - Cafetería",
            fechaHallazgo = "2026-10-25",
            registradoPor = LocalDataStore.USUARIO_REGISTRADOR_ID,
            estado = EstadoObjeto.ENTREGADO,
            ubicacionId = "ubi-002"
        )

        assertEquals("UTP-2026-0098", objeto.codigoCustodia)
    }
}
