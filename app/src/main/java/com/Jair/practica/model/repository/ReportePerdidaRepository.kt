package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.perdida.CoincidenciaSugerida
import com.Jair.practica.model.perdida.DatosReportePerdida
import com.Jair.practica.model.perdida.FiltrosCoincidencia

interface ReportePerdidaRepository {
    fun findById(id: String): ReportePerdida?
    fun listByCreatedBy(usuarioId: String): List<ReportePerdida>
    fun save(reporte: ReportePerdida): ReportePerdida
    fun update(reporte: ReportePerdida): ReportePerdida

    /** Reportes del actor que todavía se pueden reanudar (borrador o abiertos), el más reciente primero. */
    fun listPendientes(actorId: String, actorRol: LoginRole): List<ReportePerdida>

    /** RF-05: guarda (o actualiza) un BORRADOR. Solo valida formatos; no exige descripción ni contacto. */
    fun guardarBorrador(
        datos: DatosReportePerdida,
        reporteId: String?,
        actorId: String,
        actorRol: LoginRole,
        fecha: String,
        idOperacion: String
    ): ResultadoReporte

    /** RF-05: registra el reporte. Exige descripción y medio de contacto; queda "Sin coincidencia". */
    fun registrar(
        datos: DatosReportePerdida,
        reporteId: String?,
        actorId: String,
        actorRol: LoginRole,
        fecha: String,
        idOperacion: String
    ): ResultadoReporte

    /** RF-06: busca coincidencias con los filtros y CONSERVA el resultado en el reporte. */
    fun buscarCoincidencias(
        reporteId: String,
        filtros: FiltrosCoincidencia,
        actorId: String,
        actorRol: LoginRole,
        fecha: String,
        idOperacion: String
    ): ResultadoBusqueda

    /** RF-06: coincidencias ya guardadas en el reporte (las que siguen bajo custodia). */
    fun coincidenciasGuardadas(
        reporteId: String,
        actorId: String,
        actorRol: LoginRole
    ): List<CoincidenciaSugerida>

    /**
     * RF-06: el registrador decide dar seguimiento a una coincidencia ("En seguimiento").
     * Solo marca el caso; NO contacta ni entrega nada al solicitante.
     */
    fun marcarSeguimiento(
        reporteId: String,
        objetoId: String,
        actorId: String,
        actorRol: LoginRole,
        fecha: String,
        idOperacion: String
    ): ResultadoReporte
}
