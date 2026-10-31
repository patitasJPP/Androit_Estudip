package com.Jair.practica.viewmodel.perdida

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.perdida.DatosReportePerdida
import com.Jair.practica.model.perdida.ErroresReporte
import com.Jair.practica.model.perdida.permiteEditar
import com.Jair.practica.model.repository.MotivoRechazoPerdida
import com.Jair.practica.model.repository.ReportePerdidaRepository
import com.Jair.practica.model.repository.ResultadoReporte
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Estado de la pantalla "Registrar pérdida" (RF-05). La Activity solo dibuja este estado.
 *
 * @property pendientes reportes del usuario que se pueden reanudar (borradores y abiertos).
 * @property reporteActual reporte que se está editando; null = reporte nuevo.
 * @property errores mensaje de error por campo, para mostrarlo junto al campo.
 * @property mensaje aviso de un solo uso (la Activity lo muestra y llama a [mensajeConsumido]).
 */
data class RegistrarPerdidaUiState(
    val cargando: Boolean = false,
    val accesoDenegado: Boolean = false,
    val pendientes: List<ReportePerdida> = emptyList(),
    val reporteActual: ReportePerdida? = null,
    val errores: ErroresReporte = ErroresReporte(),
    val mensaje: String? = null
) {
    /** Los datos solo se editan antes de que existan coincidencias. */
    val formularioEditable: Boolean
        get() = reporteActual == null || reporteActual.estado.permiteEditar()
}

class RegistrarPerdidaViewModel(
    private val repository: ReportePerdidaRepository,
    private val actorId: String,
    private val actorRol: LoginRole,
    private val reloj: () -> String = ::fechaActual,
    private val generarId: () -> String = { UUID.randomUUID().toString() }
) : ViewModel() {

    private val _state = MutableLiveData<RegistrarPerdidaUiState>(RegistrarPerdidaUiState())
    val state: LiveData<RegistrarPerdidaUiState> = _state

    private var idOperacion: String = generarId()
    private var enProceso = false

    private fun actual(): RegistrarPerdidaUiState = _state.value ?: RegistrarPerdidaUiState()

    /**
     * Llamar en onResume: carga los reportes pendientes para poder reanudarlos y refresca el reporte
     * abierto (su estado pudo cambiar en la pantalla de coincidencias).
     */
    fun iniciar() {
        if (actorRol != LoginRole.PERSONAL_REGISTRADOR) {
            _state.value = actual().copy(accesoDenegado = true)
            return
        }
        val pendientes = repository.listPendientes(actorId, actorRol)
        val idAbierto = actual().reporteActual?.id
        _state.value = actual().copy(
            pendientes = pendientes,
            reporteActual = pendientes.firstOrNull { it.id == idAbierto } ?: actual().reporteActual
        )
    }

    /** Reanuda un reporte pendiente (borrador o ya registrado) de la lista del propio usuario. */
    fun reanudar(reporteId: String) {
        val reporte = actual().pendientes.firstOrNull { it.id == reporteId }
        if (reporte == null) {
            _state.value = actual().copy(mensaje = "No se encontró el reporte pendiente.")
            return
        }
        idOperacion = generarId()
        _state.value = actual().copy(reporteActual = reporte, errores = ErroresReporte())
    }

    /** Vuelve a un formulario en blanco para un reporte nuevo. */
    fun nuevoReporte() {
        idOperacion = generarId()
        _state.value = actual().copy(reporteActual = null, errores = ErroresReporte())
    }

    fun guardarBorrador(datos: DatosReportePerdida) = ejecutar(datos, definitivo = false)

    fun registrar(datos: DatosReportePerdida) = ejecutar(datos, definitivo = true)

    fun mensajeConsumido() {
        _state.value = actual().copy(mensaje = null)
    }

    private fun ejecutar(datos: DatosReportePerdida, definitivo: Boolean) {
        if (enProceso) return
        enProceso = true
        _state.value = actual().copy(cargando = true, errores = ErroresReporte(), mensaje = null)
        try {
            val reporteId = actual().reporteActual?.id
            val fecha = reloj()
            val resultado = if (definitivo) {
                repository.registrar(datos, reporteId, actorId, actorRol, fecha, idOperacion)
            } else {
                repository.guardarBorrador(datos, reporteId, actorId, actorRol, fecha, idOperacion)
            }
            when (resultado) {
                is ResultadoReporte.Registrada -> {
                    idOperacion = generarId() // éxito: la próxima acción es una operación distinta
                    _state.value = actual().copy(
                        reporteActual = resultado.reporte,
                        pendientes = repository.listPendientes(actorId, actorRol),
                        mensaje = if (definitivo) {
                            "Reporte registrado. Ahora puedes buscar coincidencias."
                        } else {
                            "Borrador guardado. Podrás reanudarlo después."
                        }
                    )
                }
                is ResultadoReporte.Invalida ->
                    _state.value = actual().copy(errores = resultado.errores)
                is ResultadoReporte.Denegada ->
                    _state.value = actual().copy(mensaje = textoDe(resultado.motivo))
            }
        } finally {
            enProceso = false
            _state.value = actual().copy(cargando = false)
        }
    }

    private fun textoDe(motivo: MotivoRechazoPerdida): String = when (motivo) {
        MotivoRechazoPerdida.ROL_NO_AUTORIZADO -> "Solo el Personal registrador puede registrar pérdidas."
        MotivoRechazoPerdida.REPORTE_NO_ENCONTRADO -> "No se encontró el reporte."
        MotivoRechazoPerdida.NO_ES_PROPIETARIO -> "Solo puedes modificar los reportes que tú registraste."
        MotivoRechazoPerdida.ESTADO_INVALIDO ->
            "Este reporte ya tiene coincidencias y sus datos no se pueden modificar."
        MotivoRechazoPerdida.BORRADOR_VACIO -> "Escribe algún dato antes de guardar el borrador."
        else -> "No se pudo completar la operación."
    }

    class Factory(
        private val repository: ReportePerdidaRepository,
        private val actorId: String,
        private val actorRol: LoginRole
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RegistrarPerdidaViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return RegistrarPerdidaViewModel(repository, actorId, actorRol) as T
        }
    }
}

/** Fecha y hora actuales con el formato que usan los datos del proyecto ("2026-10-22 10:24"). */
internal fun fechaActual(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
