package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.perdida.CoincidenciaSugerida
import com.Jair.practica.model.perdida.ErroresReporte

/*
 * Resultados de las operaciones de pérdidas.*/

/** Motivos por los que el repositorio rechaza una operación de pérdidas. */
enum class MotivoRechazoPerdida {
    /** Solo el Personal registrador puede registrar o consultar pérdidas. */
    ROL_NO_AUTORIZADO,
    REPORTE_NO_ENCONTRADO,
    /** Un registrador solo trabaja con los reportes que él originó. */
    NO_ES_PROPIETARIO,
    /** La operación no aplica al estado actual del reporte (p. ej. editar uno con coincidencias). */
    ESTADO_INVALIDO,
    /** Se intentó guardar un borrador sin ningún dato. */
    BORRADOR_VACIO,
    /** Se buscó sin escribir ningún filtro. */
    SIN_FILTROS,
    FILTROS_INVALIDOS,
    /** El objeto ya no está BAJO_CUSTODIA (p. ej. fue entregado entre la búsqueda y el seguimiento). */
    OBJETO_NO_DISPONIBLE,
    /** El objeto no figura entre las coincidencias sugeridas de este reporte. */
    COINCIDENCIA_NO_SUGERIDA
}

/** Resultado de guardar un borrador, registrar un reporte o dar seguimiento. */
sealed interface ResultadoReporte {
    data class Registrada(
        val reporte: ReportePerdida,
        val estadoAnterior: EstadoReportePerdida?
    ) : ResultadoReporte

    /** Los datos no pasaron la validación; [errores] trae el mensaje de cada campo. */
    data class Invalida(val errores: ErroresReporte) : ResultadoReporte

    data class Denegada(val motivo: MotivoRechazoPerdida) : ResultadoReporte
}

/** Resultado de buscar coincidencias. */
sealed interface ResultadoBusqueda {
    data class Realizada(
        val reporte: ReportePerdida,
        val coincidencias: List<CoincidenciaSugerida>,
        val estadoAnterior: EstadoReportePerdida?
    ) : ResultadoBusqueda

    data class Denegada(val motivo: MotivoRechazoPerdida) : ResultadoBusqueda
}
