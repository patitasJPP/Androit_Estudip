package com.Jair.practica.model.local

import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.EventoAuditoria
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.repository.AuditoriaRepository

class AlmacenPerdidas {
    val reportes = mutableListOf<ReportePerdida>()
    val eventosAuditoria = mutableListOf<EventoAuditoria>()

    /** idOperacion -> id del reporte al que se aplicó. Sirve para no duplicar al repetir una operación. */
    val operaciones = mutableMapOf<String, String>()

    init {
        cargarSemilla()
    }

    private fun cargarSemilla() {
        reportes.add(
            ReportePerdida(
                id = REPORTE_DEMO_ID,
                personaAtendidaId = "per-2026-0001",
                descripcion = "Laptop negra con stickers de la UTP y funda de tela.",
                lugarAproximado = "Pabellón B - Aula 302",
                fechaReporte = "2026-10-26 09:40",
                estado = EstadoReportePerdida.COINCIDENCIA_SUGERIDA,
                creadoPor = USUARIO_REGISTRADOR_ID,
                contactoNombre = "María Fernanda Quispe Rojas",
                contactoCorreo = "mquisep@edu.utp.edu.pe",
                contactoTelefono = "947123456",
                categoria = "Laptop",
                color = "Negro",
                marca = "Lenovo",
                pabellon = "B",
                piso = "3",
                aula = "302",
                coincidenciaIds = listOf("obj-2026-0001")
            )
        )
        // Borrador incompleto de ejemplo, para poder probar "reanudar reporte pendiente".
        reportes.add(
            ReportePerdida(
                id = "rep-2026-0002",
                descripcion = "Mochila azul",
                fechaReporte = "2026-10-30 16:05",
                estado = EstadoReportePerdida.BORRADOR,
                creadoPor = USUARIO_REGISTRADOR_ID,
                categoria = "Mochila",
                color = "Azul"
            )
        )
    }

    companion object {
        /** Mismo valor que usa Jair en su LocalDataStore: hasta que exista sesión real, es el registrador de prueba. */
        const val USUARIO_REGISTRADOR_ID = "usr-registrador-01"
        const val REPORTE_DEMO_ID = "rep-2026-0001"
    }
}

object AlmacenPerdidasLocal {
    val instancia = AlmacenPerdidas()
}

class AuditoriaPerdidaLocalRepository(
    private val store: AlmacenPerdidas = AlmacenPerdidasLocal.instancia
) : AuditoriaRepository {

    override fun save(evento: EventoAuditoria): EventoAuditoria {
        store.eventosAuditoria.add(evento)
        return evento
    }

    override fun listByEntity(entidadTipo: String, entidadId: String): List<EventoAuditoria> =
        store.eventosAuditoria.filter { it.entidadTipo == entidadTipo && it.entidadId == entidadId }
}
