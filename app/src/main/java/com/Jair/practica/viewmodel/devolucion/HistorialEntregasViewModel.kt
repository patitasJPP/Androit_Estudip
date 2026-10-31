package com.Jair.practica.viewmodel.devolucion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Jair.practica.model.entity.Entrega
import com.Jair.practica.model.entity.EstadoEntrega
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.PersonaAtendida
import com.Jair.practica.model.entity.Reclamacion
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.LocalEntregaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.LocalPersonaAtendidaRepository
import com.Jair.practica.model.local.LocalReclamacionRepository
import com.Jair.practica.model.repository.EntregaRepository
import com.Jair.practica.model.repository.ObjetoEncontradoRepository
import com.Jair.practica.model.repository.PersonaAtendidaRepository
import com.Jair.practica.model.repository.ReclamacionRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Contexto de presentación de una entrega dentro del historial (entrega + datos resueltos). */
data class EntregaHistorial(
    val entrega: Entrega,
    val objeto: ObjetoEncontrado?,
    val persona: PersonaAtendida?,
    val reclamacion: Reclamacion?
)

data class HistorialEntregasUiState(
    val cargando: Boolean = true,
    val errorAcceso: Boolean = false,
    val errorCarga: String? = null,
    val errorGeneral: String? = null,
    val items: List<EntregaHistorial> = emptyList(),
    val texto: String = "",
    val estadoFiltro: EstadoEntrega? = null,
    val filtroCategoria: String? = null,
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val errorFecha: Boolean = false,
    val listaVacia: Boolean = false
)

class HistorialEntregasViewModel(
    private val entregaRepository: EntregaRepository = LocalEntregaRepository(),
    private val objetoRepository: ObjetoEncontradoRepository = LocalObjetoEncontradoRepository(),
    private val personaRepository: PersonaAtendidaRepository = LocalPersonaAtendidaRepository(),
    private val reclamacionRepository: ReclamacionRepository = LocalReclamacionRepository(),
    private val actorId: String = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
    private val actorRol: LoginRole = LoginRole.ADMINISTRADOR_SAE
) : ViewModel() {

    private val _state = MutableLiveData<HistorialEntregasUiState>(HistorialEntregasUiState())
    val state: LiveData<HistorialEntregasUiState> = _state

    fun cargar(actorRol: LoginRole) {
        val estadoActual = _state.value
        _state.value = estadoActual?.copy(
            cargando = true,
            errorAcceso = false,
            errorCarga = null,
            errorGeneral = null,
            items = emptyList(),
            texto = "",
            estadoFiltro = null,
            filtroCategoria = null,
            fechaInicio = "",
            fechaFin = "",
            errorFecha = false,
            listaVacia = false
        )

        if (actorRol != LoginRole.ADMINISTRADOR_SAE) {
            _state.value = _state.value?.copy(
                cargando = false,
                errorAcceso = true,
                errorGeneral = "La pantalla de historial de entregas solo esta disponible para el rol Administrador SAE."
            )
            return
        }

        val entregas = entregaRepository.listByAdministrador(actorRol)
        val items = entregas.map { entrega ->
            EntregaHistorial(
                entrega = entrega,
                objeto = objetoRepository.findById(entrega.objetoEncontradoId),
                persona = personaRepository.findById(entrega.receptorId),
                reclamacion = reclamacionRepository.findById(entrega.reclamacionId)
            )
        }

        _state.value = _state.value?.copy(
            cargando = false,
            items = items,
            listaVacia = items.isEmpty()
        )
    }

    fun onTextoChange(texto: String) {
        _state.value = _state.value?.copy(texto = texto.trim())
        aplicarFiltros()
    }

    fun onFiltroReceptorChange(receptor: String) {
        _state.value = _state.value?.copy(texto = receptor.trim())
        aplicarFiltros()
    }

    fun onEstadoChange(estado: EstadoEntrega?) {
        _state.value = _state.value?.copy(estadoFiltro = estado)
        aplicarFiltros()
    }

    fun onCategoriaChange(categoria: String?) {
        _state.value = _state.value?.copy(
            filtroCategoria = categoria?.trim()?.takeIf { it?.isNotEmpty() == true }
        )
        aplicarFiltros()
    }

    fun onFechaInicioChange(fecha: String) {
        _state.value = _state.value?.copy(fechaInicio = fecha.trim())
        aplicarFiltros()
    }

    fun onFechaFinChange(fecha: String) {
        _state.value = _state.value?.copy(fechaFin = fecha.trim())
        aplicarFiltros()
    }

    fun limpiarFiltros() {
        _state.value = _state.value?.copy(
            texto = "",
            estadoFiltro = null,
            filtroCategoria = null,
            fechaInicio = "",
            fechaFin = "",
            errorFecha = false
        )
        aplicarFiltros()
    }

    fun onChipEstaSemana() {
        val calendario = Calendar.getInstance()
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        calendario.add(Calendar.DAY_OF_WEEK, -((calendario.get(Calendar.DAY_OF_WEEK) + 5) % 7))
        val inicio = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES")).format(calendario.time)
        calendario.add(Calendar.DAY_OF_WEEK, 6)
        val fin = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES")).format(calendario.time)
        _state.value = _state.value?.copy(fechaInicio = inicio, fechaFin = fin)
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val estadoActual = _state.value ?: return
        val errorFecha = !estadoActual.fechaInicio.isNullOrBlank() &&
            !estadoActual.fechaFin.isNullOrBlank() &&
            estadoActual.fechaInicio > estadoActual.fechaFin

        val items = estadoActual.items.filter { item ->
            val entrega = item.entrega
            val texto = estadoActual.texto.trim().lowercase()
            val coincidencias = mutableListOf<String>()
            item.objeto?.codigoCustodia?.let { coincidencias.add(it) }
            item.objeto?.descripcion?.let { coincidencias.add(it) }
            item.objeto?.categoria?.let { coincidencias.add(it) }
            item.persona?.nombre?.let { coincidencias.add(it) }
            item.persona?.identificador?.let { coincidencias.add(it) }

            val cumpleTexto = texto.isEmpty() || coincidencias.any { it.lowercase().contains(texto) }
            val cumpleEstado = estadoActual.estadoFiltro == null || entrega.estado == estadoActual.estadoFiltro
            val cumpleCategoria = estadoActual.filtroCategoria.isNullOrBlank() ||
                item.objeto?.categoria?.contains(estadoActual.filtroCategoria!!, ignoreCase = true) == true
            val cumpleFechas = when {
                estadoActual.fechaInicio.isBlank() || estadoActual.fechaFin.isBlank() -> true
                else -> {
                    val fechaEntrega = entrega.fechaConfirmacion ?: ""
                    val diaEntrega = fechaEntrega.substringBefore(' ').takeIf { it.length == 10 } ?: ""
                    diaEntrega >= estadoActual.fechaInicio && diaEntrega <= estadoActual.fechaFin
                }
            }

            cumpleTexto && cumpleEstado && cumpleCategoria && cumpleFechas
        }

        _state.value = estadoActual.copy(
            items = items,
            errorFecha = errorFecha,
            listaVacia = items.isEmpty()
        )
    }

    companion object {
        fun etiquetaEstadoEntrega(estado: EstadoEntrega?): String = when (estado) {
            EstadoEntrega.CONFIRMADA -> "Entrega confirmada"
            EstadoEntrega.RESERVADA -> "Reservada"
            EstadoEntrega.INCIERTA -> "Incierta"
            EstadoEntrega.CANCELADA -> "Cancelada"
            null -> ""
        }

        fun enmascararDni(dni: String?): String {
            val valor = dni?.trim() ?: return ""
            return if (valor.length <= 4) "****${valor}" else "****${valor.takeLast(4)}"
        }

        fun fechaActual(): String =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("es", "ES")).format(Date())
    }

    class Factory(
        private val entregaRepository: EntregaRepository,
        private val objetoRepository: ObjetoEncontradoRepository,
        private val personaRepository: PersonaAtendidaRepository,
        private val reclamacionRepository: ReclamacionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(HistorialEntregasViewModel::class.java)) {
                throw IllegalArgumentException("Clase no valida: ${modelClass.name}")
            }
            @Suppress("UNCHECKED_CAST")
            return HistorialEntregasViewModel(
                entregaRepository = entregaRepository,
                objetoRepository = objetoRepository,
                personaRepository = personaRepository,
                reclamacionRepository = reclamacionRepository,
                actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
                actorRol = LoginRole.ADMINISTRADOR_SAE
            ) as T
        }
    }
}
