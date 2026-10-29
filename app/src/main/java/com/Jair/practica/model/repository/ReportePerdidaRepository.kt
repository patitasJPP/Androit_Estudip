package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.ReportePerdida

interface ReportePerdidaRepository {
    fun findById(id: String): ReportePerdida?
    fun listByCreatedBy(usuarioId: String): List<ReportePerdida>
    fun save(reporte: ReportePerdida): ReportePerdida
    fun update(reporte: ReportePerdida): ReportePerdida
}
