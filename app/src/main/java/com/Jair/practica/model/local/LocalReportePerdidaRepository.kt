package com.Jair.practica.model.local

import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.EventoAuditoria
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.perdida.CoincidenciaSugerida
import com.Jair.practica.model.perdida.DatosReportePerdida
import com.Jair.practica.model.perdida.ErrorFiltros
import com.Jair.practica.model.perdida.FiltrosCoincidencia
import com.Jair.practica.model.perdida.MotorCoincidencias
import com.Jair.practica.model.perdida.ValidadorReportePerdida
import com.Jair.practica.model.perdida.estaAbierto
import com.Jair.practica.model.perdida.permiteBuscar
import com.Jair.practica.model.perdida.permiteEditar
import com.Jair.practica.model.repository.AuditoriaRepository
import com.Jair.practica.model.repository.MotivoRechazoPerdida
import com.Jair.practica.model.repository.ObjetoEncontradoRepository
import com.Jair.practica.model.repository.ReportePerdidaRepository
import com.Jair.practica.model.repository.ResultadoBusqueda
import com.Jair.practica.model.repository.ResultadoReporte

class LocalReportePerdidaRepository(
    private val store: AlmacenPerdidas = AlmacenPerdidasLocal.instancia,
    private val objetos: ObjetoEncontradoRepository = ObjetoEncontradoDemoRepository.instancia,
    private val auditoria: AuditoriaRepository = AuditoriaPerdidaLocalRepository(store)
) : ReportePerdidaRepository {

    // ---------- Acceso simple (sin reglas de negocio; las pantallas usan las operaciones de abajo) ----------

    override fun findById(id: String): ReportePerdida? = store.reportes.firstOrNull { it.id == id }

    override fun listByCreatedBy(usuarioId: String): List<ReportePerdida> =
        store.reportes.filter { it.creadoPor == usuarioId }

    override fun save(reporte: ReportePerdida): ReportePerdida {
        val index = store.reportes.indexOfFirst { it.id == reporte.id }
        if (index >= 0) store.reportes[index] = reporte else store.reportes.add(reporte)
        return reporte
    }

    override fun update(reporte: ReportePerdida): ReportePerdida {
        val index = store.reportes.indexOfFirst { it.id == reporte.id }
        if (index >= 0) store.reportes[index] = reporte
        return reporte
    }

    // ---------- RF-05 ----------

    override fun listPendientes(actorId: String, actorRol: LoginRole): List<ReportePerdida> {
        if (actorRol != LoginRole.PERSONAL_REGISTRADOR) return emptyList()
        return store.reportes
            .filter { it.creadoPor == actorId && it.estado.estaAbierto() }
            .sortedWith(compareByDescending<ReportePerdida> { it.fechaReporte }.thenByDescending { it.id })
    }

    override fun guardarBorrador(
        datos: DatosReportePerdida,
        reporteId: String?,
        actorId: String,
        actorRol: LoginRole,
        fecha: String,
        idOperacion: String
    ): ResultadoReporte {
        if (actorRol != LoginRole.PERSONAL_REGISTRADOR) {
            return ResultadoReporte.Denegada(MotivoRechazoPerdida.ROL_NO_AUTORIZADO)
        }
        operacionRepetida(idOperacion)?.let { return ResultadoReporte.Registrada(it, null) }
        if (datos.estaVacio()) return ResultadoReporte.Denegada(MotivoRechazoPerdida.BORRADOR_VACIO)
        val errores = ValidadorReportePerdida.validarBorrador(datos)
        if (errores.hayErrores) return ResultadoReporte.Invalida(errores)
        return guardar(
            datos, reporteId, EstadoReportePerdida.BORRADOR, actorId, fecha, idOperacion,
            accion = "BORRADOR_GUARDADO", motivo = "Borrador de reporte de pérdida guardado."
        )
    }

    override fun registrar(
        datos: DatosReportePerdida,
        reporteId: String?,
        actorId: String,
        actorRol: LoginRole,
        fecha: String,
        idOperacion: String
    ): ResultadoReporte {
        if (actorRol != LoginRole.PERSONAL_REGISTRADOR) {
            return ResultadoReporte.Denegada(MotivoRechazoPerdida.ROL_NO_AUTORIZADO)
        }
        operacionRepetida(idOperacion)?.let { return ResultadoReporte.Registrada(it, null) }
        val errores = ValidadorReportePerdida.validarRegistro(datos)
        if (errores.hayErrores) return ResultadoReporte.Invalida(errores)
        return guardar(
            datos, reporteId, EstadoReportePerdida.SIN_COINCIDENCIA, actorId, fecha, idOperacion,
            accion = "REPORTE_PERDIDA_REGISTRADO", motivo = "Pérdida comunicada presencialmente."
        )
    }

    /** Crea el reporte o actualiza el existente (reanudar) con los datos escritos. */
    private fun guardar(
        datos: DatosReportePerdida,
        reporteId: String?,
        estadoDestino: EstadoReportePerdida,
        actorId: String,
        fecha: String,
        idOperacion: String,
        accion: String,
        motivo: String
    ): ResultadoReporte {
        val actual: ReportePerdida? = if (reporteId == null) {
            null
        } else {
            store.reportes.firstOrNull { it.id == reporteId }
                ?: return ResultadoReporte.Denegada(MotivoRechazoPerdida.REPORTE_NO_ENCONTRADO)
        }
        if (actual != null) {
            if (actual.creadoPor != actorId) {
                return ResultadoReporte.Denegada(MotivoRechazoPerdida.NO_ES_PROPIETARIO)
            }
            // No se editan datos de un reporte que ya tiene coincidencias o seguimiento.
            if (!actual.estado.permiteEditar()) {
                return ResultadoReporte.Denegada(MotivoRechazoPerdida.ESTADO_INVALIDO)
            }
            // Un reporte ya registrado no vuelve a ser borrador.
            if (estadoDestino == EstadoReportePerdida.BORRADOR && actual.estado != EstadoReportePerdida.BORRADOR) {
                return ResultadoReporte.Denegada(MotivoRechazoPerdida.ESTADO_INVALIDO)
            }
        }

        val base = actual ?: ReportePerdida(
            id = nuevoIdReporte(fecha), descripcion = "", fechaReporte = fecha, creadoPor = actorId
        )
        val nuevo = base.copy(
            descripcion = datos.descripcion.trim(),
            lugarAproximado = datos.lugarAproximado.nullSiVacio(),
            categoria = datos.categoria.nullSiVacio(),
            color = datos.color.nullSiVacio(),
            marca = datos.marca.nullSiVacio(),
            modelo = datos.modelo.nullSiVacio(),
            pabellon = datos.pabellon.nullSiVacio(),
            piso = datos.piso.nullSiVacio(),
            aula = datos.aula.nullSiVacio(),
            contactoNombre = datos.contactoNombre.nullSiVacio(),
            contactoCorreo = datos.contactoCorreo.nullSiVacio(),
            contactoTelefono = datos.contactoTelefono.nullSiVacio()
                ?.let { ValidadorReportePerdida.limpiarTelefono(it) },
            estado = estadoDestino,
            idOperacion = idOperacion.nullSiVacio()
        )
        save(nuevo)
        recordarOperacion(idOperacion, nuevo.id)
        auditar(accion, nuevo.id, actorId, fecha, actual?.estado, nuevo.estado, motivo)
        return ResultadoReporte.Registrada(nuevo, actual?.estado)
    }

    // ---------- RF-06 ----------

    override fun buscarCoincidencias(
        reporteId: String,
        filtros: FiltrosCoincidencia,
        actorId: String,
        actorRol: LoginRole,
        fecha: String,
        idOperacion: String
    ): ResultadoBusqueda {
        if (actorRol != LoginRole.PERSONAL_REGISTRADOR) {
            return ResultadoBusqueda.Denegada(MotivoRechazoPerdida.ROL_NO_AUTORIZADO)
        }
        val actual = store.reportes.firstOrNull { it.id == reporteId }
            ?: return ResultadoBusqueda.Denegada(MotivoRechazoPerdida.REPORTE_NO_ENCONTRADO)
        if (actual.creadoPor != actorId) {
            return ResultadoBusqueda.Denegada(MotivoRechazoPerdida.NO_ES_PROPIETARIO)
        }
        // Operación repetida: se devuelve el mismo resultado ya guardado, sin duplicar nada.
        operacionRepetida(idOperacion)?.let {
            return ResultadoBusqueda.Realizada(it, vistasDisponibles(it), null)
        }
        if (!actual.estado.permiteBuscar()) {
            return ResultadoBusqueda.Denegada(MotivoRechazoPerdida.ESTADO_INVALIDO)
        }
        when (ValidadorReportePerdida.validarFiltros(filtros)) {
            ErrorFiltros.SIN_FILTROS ->
                return ResultadoBusqueda.Denegada(MotivoRechazoPerdida.SIN_FILTROS)
            ErrorFiltros.FUERA_DE_CATALOGO ->
                return ResultadoBusqueda.Denegada(MotivoRechazoPerdida.FILTROS_INVALIDOS)
            null -> Unit
        }

        val coincidencias = MotorCoincidencias.buscar(objetos.list(), filtros)
        // Estados de la guía: sin coincidencia / coincidencia sugerida / en seguimiento.
        // Si el caso ya está EN_SEGUIMIENTO se conserva; buscar de nuevo no lo desmarca.
        val nuevoEstado = when (actual.estado) {
            EstadoReportePerdida.EN_SEGUIMIENTO -> EstadoReportePerdida.EN_SEGUIMIENTO
            else ->
                if (coincidencias.isEmpty()) EstadoReportePerdida.SIN_COINCIDENCIA
                else EstadoReportePerdida.COINCIDENCIA_SUGERIDA
        }
        val ids = coincidencias.map { it.objetoId }
        val nuevo = actual.copy(
            coincidenciaIds = ids,
            estado = nuevoEstado,
            idOperacion = idOperacion.nullSiVacio()
        )
        save(nuevo)
        recordarOperacion(idOperacion, nuevo.id)
        if (nuevo.estado != actual.estado || ids != actual.coincidenciaIds) {
            auditar(
                "COINCIDENCIAS_ACTUALIZADAS", nuevo.id, actorId, fecha, actual.estado, nuevo.estado,
                "Búsqueda de coincidencias: ${ids.size} sugerida(s). Son sugerencias, no autorizan entrega."
            )
        }
        return ResultadoBusqueda.Realizada(nuevo, coincidencias, actual.estado)
    }

    override fun coincidenciasGuardadas(
        reporteId: String,
        actorId: String,
        actorRol: LoginRole
    ): List<CoincidenciaSugerida> {
        if (actorRol != LoginRole.PERSONAL_REGISTRADOR) return emptyList()
        val reporte = store.reportes.firstOrNull { it.id == reporteId } ?: return emptyList()
        if (reporte.creadoPor != actorId) return emptyList()
        return vistasDisponibles(reporte)
    }

    override fun marcarSeguimiento(
        reporteId: String,
        objetoId: String,
        actorId: String,
        actorRol: LoginRole,
        fecha: String,
        idOperacion: String
    ): ResultadoReporte {
        if (actorRol != LoginRole.PERSONAL_REGISTRADOR) {
            return ResultadoReporte.Denegada(MotivoRechazoPerdida.ROL_NO_AUTORIZADO)
        }
        val actual = store.reportes.firstOrNull { it.id == reporteId }
            ?: return ResultadoReporte.Denegada(MotivoRechazoPerdida.REPORTE_NO_ENCONTRADO)
        if (actual.creadoPor != actorId) {
            return ResultadoReporte.Denegada(MotivoRechazoPerdida.NO_ES_PROPIETARIO)
        }
        operacionRepetida(idOperacion)?.let { return ResultadoReporte.Registrada(it, null) }
        // Ya está en seguimiento de este mismo objeto: no se repite el cambio.
        if (actual.estado == EstadoReportePerdida.EN_SEGUIMIENTO && actual.objetoSeguimientoId == objetoId) {
            return ResultadoReporte.Registrada(actual, actual.estado)
        }
        if (actual.estado != EstadoReportePerdida.COINCIDENCIA_SUGERIDA &&
            actual.estado != EstadoReportePerdida.EN_SEGUIMIENTO
        ) {
            return ResultadoReporte.Denegada(MotivoRechazoPerdida.ESTADO_INVALIDO)
        }
        if (objetoId !in actual.coincidenciaIds) {
            return ResultadoReporte.Denegada(MotivoRechazoPerdida.COINCIDENCIA_NO_SUGERIDA)
        }
        // Se vuelve a comprobar el estado físico: pudo entregarse después de la búsqueda.
        val objeto = objetos.findById(objetoId)
        if (objeto == null || objeto.estado != EstadoObjeto.BAJO_CUSTODIA) {
            return ResultadoReporte.Denegada(MotivoRechazoPerdida.OBJETO_NO_DISPONIBLE)
        }

        val nuevo = actual.copy(
            estado = EstadoReportePerdida.EN_SEGUIMIENTO,
            objetoSeguimientoId = objetoId,
            idOperacion = idOperacion.nullSiVacio()
        )
        save(nuevo)
        recordarOperacion(idOperacion, nuevo.id)
        auditar(
            "SEGUIMIENTO_INICIADO", nuevo.id, actorId, fecha, actual.estado, nuevo.estado,
            "Seguimiento de la coincidencia ${objeto.codigoCustodia}. No se contactó ni entregó al solicitante."
        )
        return ResultadoReporte.Registrada(nuevo, actual.estado)
    }


    /** Vistas públicas de las coincidencias guardadas que siguen bajo custodia. */
    private fun vistasDisponibles(reporte: ReportePerdida): List<CoincidenciaSugerida> =
        reporte.coincidenciaIds
            .mapNotNull { objetos.findById(it) }
            .filter { it.estado == EstadoObjeto.BAJO_CUSTODIA }
            .map { MotorCoincidencias.aVistaPublica(it) }

    /** Si este idOperacion ya se aplicó, devuelve el reporte al que se aplicó. */
    private fun operacionRepetida(idOperacion: String): ReportePerdida? {
        if (idOperacion.isBlank()) return null
        val reporteId = store.operaciones[idOperacion] ?: return null
        return store.reportes.firstOrNull { it.id == reporteId }
    }

    private fun recordarOperacion(idOperacion: String, reporteId: String) {
        if (idOperacion.isNotBlank()) store.operaciones[idOperacion] = reporteId
    }

    /** Id con el formato del proyecto: rep-AAAA-0001 (el año sale de la fecha recibida). */
    private fun nuevoIdReporte(fecha: String): String {
        val anio = fecha.take(4).takeIf { it.length == 4 && it.all(Char::isDigit) } ?: "0000"
        var numero = store.reportes.size + 1
        var id: String
        do {
            id = "rep-$anio-${numero.toString().padStart(4, '0')}"
            numero++
        } while (store.reportes.any { it.id == id })
        return id
    }

    /** Registra usuario, fecha, estado anterior, estado nuevo y motivo (guía: controles de auditoría). */
    private fun auditar(
        accion: String,
        reporteId: String,
        actorId: String,
        fecha: String,
        anterior: EstadoReportePerdida?,
        nuevo: EstadoReportePerdida,
        motivo: String
    ) {
        auditoria.save(
            EventoAuditoria(
                id = "aud-perdida-${java.util.UUID.randomUUID()}",
                entidadTipo = "ReportePerdida",
                entidadId = reporteId,
                accion = accion,
                usuarioId = actorId,
                fecha = fecha,
                detalle = "Estado anterior: ${anterior?.name ?: "-"}. Estado nuevo: ${nuevo.name}. Motivo: $motivo"
            )
        )
    }

    private fun String.nullSiVacio(): String? = trim().ifEmpty { null }
}
