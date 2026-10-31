package com.Jair.practica.model.local

import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.repository.ReportePerdidaRepository
import java.util.UUID

class LocalReportePerdidaRepository : ReportePerdidaRepository {
    private val reportes get() = AlmacenLocal.instancia.reportesPerdida

    override fun findById(id: String): ReportePerdida? = reportes.firstOrNull { it.id == id }

    override fun listByCreatedBy(usuarioId: String): List<ReportePerdida> =
        reportes.filter { it.creadoPor == usuarioId }.toList()

    override fun save(reporte: ReportePerdida): ReportePerdida {
        val guardado = if (reporte.id.isBlank()) reporte.copy(id = UUID.randomUUID().toString()) else reporte
        reportes.removeAll { it.id == guardado.id }
        reportes.add(guardado)
        return guardado
    }

    override fun update(reporte: ReportePerdida): ReportePerdida {
        require(reportes.any { it.id == reporte.id }) { "El reporte de pérdida no existe" }
        reportes.replaceAll { if (it.id == reporte.id) reporte else it }
        return reporte
    }
}
