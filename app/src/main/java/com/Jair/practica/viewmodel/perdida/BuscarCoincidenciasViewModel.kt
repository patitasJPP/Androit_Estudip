package com.Jair.practica.viewmodel.perdida

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.perdida.CoincidenciaSugerida
import com.Jair.practica.model.perdida.FiltrosCoincidencia
import com.Jair.practica.model.perdida.permiteBuscar
import com.Jair.practica.model.repository.MotivoRechazoPerdida
import com.Jair.practica.model.repository.ReportePerdidaRepository
import com.Jair.practica.model.repository.ResultadoBusqueda
import com.Jair.practica.model.repository.ResultadoReporte
import java.util.UUID

/**
 * Estado de la pantalla "Buscar coincidencias" (RF-06).
 *
 * @property reporte caso sobre el que se busca (null si no existe o no es del usuario).
 * @property filtros valores iniciales de los filtros (se rellenan con los datos del reporte).
 * @property coincidencias sugerencias actuales. SON SOLO SUGERENCIAS: no autorizan ninguna entrega.
 * @property busquedaRealizada distingue "aún no buscó" de "buscó y no hubo resultados".
 * @property bloqueado true si el reporte aún es borrador y no se puede buscar.
 */
data class BuscarCoincidenciasUiState(
    val cargando: Boolean = true,
    val accesoDenegado: Boolean = false,
    val reporteNoEncontrado: Boolean = false,
    val bloqueado: Boolean = false,
    val reporte: ReportePerdida? = null,
    val filtros: FiltrosCoincidencia = FiltrosCoincidencia(),
    val coincidencias: List<CoincidenciaSugerida> = emptyList(),
    val busquedaRealizada: Boolean = false,
    val errorFiltros: String? = null,
    val mensaje: String? = null
)

/**
 * ViewModel de RF-06. Mismo patrón que [RegistrarPerdidaViewModel].
 * No contacta ni entrega objetos: darSeguimiento solo cambia el estado del caso a "En seguimiento".
 */
class BuscarCoincidenciasViewModel(
    private val repository: ReportePerdidaRepository,
    private val actorId: String,
    private val actorRol: LoginRole,
    private val reloj: () -> String = ::fechaActual,
    private val generarId: () -> String = { UUID.randomUUID().toString() }
) : ViewModel() {

    private val _state = MutableLiveData<BuscarCoincidenciasUiState>(BuscarCoincidenciasUiState())
    val state: LiveData<BuscarCoincidenciasUiState> = _state

    private var enProceso = false

    private fun actual(): BuscarCoincidenciasUiState = _state.value ?: BuscarCoincidenciasUiState()

    /** Carga el reporte, rellena los filtros con sus datos y recupera las coincidencias ya guardadas. */
    fun cargar(reporteId: String) {
        if (actorRol != LoginRole.PERSONAL_REGISTRADOR) {
            _state.value = actual().copy(cargando = false, accesoDenegado = true)
            return
        }
        val reporte = repository.listPendientes(actorId, actorRol).firstOrNull { it.id == reporteId }
        if (reporte == null) {
            _state.value = actual().copy(cargando = false, reporteNoEncontrado = true)
            return
        }
        val guardadas = repository.coincidenciasGuardadas(reporteId, actorId, actorRol)
        _state.value = actual().copy(
            cargando = false,
            reporte = reporte,
            bloqueado = !reporte.estado.permiteBuscar(),
            filtros = filtrosDe(reporte),
            coincidencias = guardadas,
            busquedaRealizada = guardadas.isNotEmpty()
        )
    }

    fun buscar(filtros: FiltrosCoincidencia) {
        val reporte = actual().reporte ?: return
        if (enProceso) return
        enProceso = true
        _state.value = actual().copy(cargando = true, errorFiltros = null, mensaje = null, filtros = filtros)
        try {
            when (val r = repository.buscarCoincidencias(
                reporte.id, filtros, actorId, actorRol, reloj(), generarId()
            )) {
                is ResultadoBusqueda.Realizada -> _state.value = actual().copy(
                    reporte = r.reporte,
                    coincidencias = r.coincidencias,
                    busquedaRealizada = true
                )
                is ResultadoBusqueda.Denegada -> when (r.motivo) {
                    MotivoRechazoPerdida.SIN_FILTROS ->
                        _state.value = actual().copy(errorFiltros = "Selecciona o escribe al menos un filtro.")
                    MotivoRechazoPerdida.FILTROS_INVALIDOS ->
                        _state.value = actual().copy(errorFiltros = "Alguno de los filtros no es válido.")
                    else -> _state.value = actual().copy(mensaje = textoDe(r.motivo))
                }
            }
        } finally {
            enProceso = false
            _state.value = actual().copy(cargando = false)
        }
    }

    /** Deja los filtros en blanco (no borra el resultado guardado del caso). */
    fun limpiarFiltros() {
        _state.value = actual().copy(filtros = FiltrosCoincidencia(), errorFiltros = null)
    }

    /** El registrador decide seguir esta coincidencia. NO contacta ni entrega nada. */
    fun darSeguimiento(objetoId: String) {
        val reporte = actual().reporte ?: return
        if (enProceso) return
        enProceso = true
        try {
            when (val r = repository.marcarSeguimiento(
                reporte.id, objetoId, actorId, actorRol, reloj(), generarId()
            )) {
                is ResultadoReporte.Registrada -> _state.value = actual().copy(
                    reporte = r.reporte,
                    mensaje = "Caso en seguimiento. La coincidencia es solo una sugerencia: " +
                        "la entrega la decide el Administrador SAE."
                )
                is ResultadoReporte.Invalida -> Unit
                is ResultadoReporte.Denegada ->
                    _state.value = actual().copy(mensaje = textoDe(r.motivo))
            }
        } finally {
            enProceso = false
        }
    }

    fun mensajeConsumido() {
        _state.value = actual().copy(mensaje = null)
    }

    private fun filtrosDe(r: ReportePerdida) = FiltrosCoincidencia(
        pabellon = r.pabellon.orEmpty(),
        piso = r.piso.orEmpty(),
        aula = r.aula.orEmpty(),
        categoria = r.categoria.orEmpty(),
        color = r.color.orEmpty(),
        marca = r.marca.orEmpty(),
        modelo = r.modelo.orEmpty()
    )

    private fun textoDe(motivo: MotivoRechazoPerdida): String = when (motivo) {
        MotivoRechazoPerdida.ROL_NO_AUTORIZADO -> "Solo el Personal registrador puede consultar coincidencias."
        MotivoRechazoPerdida.REPORTE_NO_ENCONTRADO -> "No se encontró el reporte."
        MotivoRechazoPerdida.NO_ES_PROPIETARIO -> "Solo puedes trabajar con los reportes que tú registraste."
        MotivoRechazoPerdida.ESTADO_INVALIDO -> "Registra el reporte antes de buscar coincidencias."
        MotivoRechazoPerdida.OBJETO_NO_DISPONIBLE ->
            "Ese objeto ya no está bajo custodia, así que no se puede dar seguimiento."
        MotivoRechazoPerdida.COINCIDENCIA_NO_SUGERIDA -> "Ese objeto no figura entre las coincidencias."
        else -> "No se pudo completar la operación."
    }

    class Factory(
        private val repository: ReportePerdidaRepository,
        private val actorId: String,
        private val actorRol: LoginRole
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(BuscarCoincidenciasViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return BuscarCoincidenciasViewModel(repository, actorId, actorRol) as T
        }
    }
}
