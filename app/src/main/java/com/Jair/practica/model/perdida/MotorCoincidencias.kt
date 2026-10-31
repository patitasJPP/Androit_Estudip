package com.Jair.practica.model.perdida

import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.ObjetoEncontrado

/**
 * Búsqueda de posibles coincidencias (RF-06).
*/
object MotorCoincidencias {

    fun buscar(
        objetos: List<ObjetoEncontrado>,
        filtros: FiltrosCoincidencia
    ): List<CoincidenciaSugerida> =
        objetos.asSequence()
            .filter { it.estado == EstadoObjeto.BAJO_CUSTODIA }
            .filter { cumple(it, filtros) }
            .sortedBy { it.codigoCustodia }
            .map { aVistaPublica(it) }
            .toList()

    /** Convierte un objeto en la vista que puede ver el registrador. */
    fun aVistaPublica(objeto: ObjetoEncontrado): CoincidenciaSugerida = CoincidenciaSugerida(
        objetoId = objeto.id,
        codigoCustodia = objeto.codigoCustodia,
        categoria = objeto.categoria,
        descripcion = objeto.descripcion,
        color = objeto.color,
        marca = objeto.marca,
        modelo = objeto.modelo,
        pabellon = objeto.pabellon,
        piso = objeto.piso,
        aula = objeto.aula,
        lugarHallazgo = objeto.lugarHallazgo,
        fechaHallazgo = objeto.fechaHallazgo
    )

    private fun cumple(o: ObjetoEncontrado, f: FiltrosCoincidencia): Boolean =
        igual(o.pabellon, f.pabellon) &&
            igual(o.piso, f.piso) &&
            contiene(o.aula, f.aula) &&
            igual(o.categoria, f.categoria) &&
            igual(o.color, f.color) &&
            contiene(o.marca, f.marca) &&
            contiene(o.modelo, f.modelo)

    private fun igual(valorObjeto: String?, filtro: String): Boolean =
        filtro.isBlank() || TextoPerdida.normalizar(valorObjeto) == TextoPerdida.normalizar(filtro)

    private fun contiene(valorObjeto: String?, filtro: String): Boolean =
        filtro.isBlank() || TextoPerdida.normalizar(valorObjeto).contains(TextoPerdida.normalizar(filtro))
}

/** Nombre oficial del estado para mostrarlo en pantalla (los de la guía). */
fun EstadoReportePerdida.etiqueta(): String = when (this) {
    EstadoReportePerdida.BORRADOR -> "Borrador"
    EstadoReportePerdida.SIN_COINCIDENCIA -> "Sin coincidencia"
    EstadoReportePerdida.COINCIDENCIA_SUGERIDA -> "Coincidencia sugerida"
    EstadoReportePerdida.EN_SEGUIMIENTO -> "En seguimiento"
    EstadoReportePerdida.RECUPERADO -> "Recuperado"
    EstadoReportePerdida.CERRADO -> "Cerrado"
    EstadoReportePerdida.ANULADO -> "Anulado"
}

/** Un reporte "pendiente" es el que todavía se puede reanudar o seguir (no está cerrado). */
fun EstadoReportePerdida.estaAbierto(): Boolean = when (this) {
    EstadoReportePerdida.BORRADOR,
    EstadoReportePerdida.SIN_COINCIDENCIA,
    EstadoReportePerdida.COINCIDENCIA_SUGERIDA,
    EstadoReportePerdida.EN_SEGUIMIENTO -> true
    else -> false
}

/** Los datos del reporte solo se pueden editar antes de que existan coincidencias. */
fun EstadoReportePerdida.permiteEditar(): Boolean =
    this == EstadoReportePerdida.BORRADOR || this == EstadoReportePerdida.SIN_COINCIDENCIA

/** Se puede buscar coincidencias solo si el reporte ya fue registrado (no es borrador) y sigue abierto. */
fun EstadoReportePerdida.permiteBuscar(): Boolean =
    this == EstadoReportePerdida.SIN_COINCIDENCIA ||
        this == EstadoReportePerdida.COINCIDENCIA_SUGERIDA ||
        this == EstadoReportePerdida.EN_SEGUIMIENTO
