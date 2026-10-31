package com.Jair.practica.viewmodel.devolucion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Jair.practica.model.entity.Entrega
import com.Jair.practica.model.entity.EstadoEntrega
import com.Jair.practica.model.entity.EventoAuditoria
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.PersonaAtendida
import com.Jair.practica.model.entity.Usuario
import com.Jair.practica.model.local.LocalAuditoriaRepository
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.LocalEntregaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.LocalPersonaAtendidaRepository
import com.Jair.practica.model.local.LocalUsuarioRepository
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.model.repository.AuditoriaRepository
import com.Jair.practica.model.repository.EntregaRepository
import com.Jair.practica.model.repository.ObjetoEncontradoRepository
import com.Jair.practica.model.repository.PersonaAtendidaRepository
import com.Jair.practica.model.repository.UsuarioRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Datos resueltos de la entrega que conforman la pantalla del acta. */
data class ActaEntrega(
    val entrega: Entrega,
    val objeto: ObjetoEncontrado?,
    val persona: PersonaAtendida?,
    val administrador: Usuario?
)

data class ActaEntregaUiState(
    val cargando: Boolean = true,
    val errorAcceso: Boolean = false,
    val errorCarga: String? = null,
    val errorGeneral: String? = null,
    val data: ActaEntrega? = null
)

class ActaEntregaViewModel(
    private val entregaRepository: EntregaRepository = LocalEntregaRepository(),
    private val usuarioRepository: UsuarioRepository = LocalUsuarioRepository(),
    private val objetoRepository: ObjetoEncontradoRepository = LocalObjetoEncontradoRepository(),
    private val personaRepository: PersonaAtendidaRepository = LocalPersonaAtendidaRepository(),
    private val auditoriaRepository: AuditoriaRepository = LocalAuditoriaRepository(),
    private val actorId: String = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
    private val actorRol: LoginRole = LoginRole.ADMINISTRADOR_SAE
) : ViewModel() {

    private val _state = MutableLiveData<ActaEntregaUiState>(ActaEntregaUiState())
    val state: LiveData<ActaEntregaUiState> = _state

    fun cargar(idEntrega: String, actorRol: LoginRole) {
        val estadoActual = _state.value
        _state.value = estadoActual?.copy(
            cargando = true,
            errorAcceso = false,
            errorCarga = null,
            errorGeneral = null,
            data = null
        )

        if (actorRol != LoginRole.ADMINISTRADOR_SAE) {
            _state.value = _state.value?.copy(
                cargando = false,
                errorAcceso = true,
                errorGeneral = "La pantalla del acta de entrega solo esta disponible para el rol Administrador SAE."
            )
            return
        }

        val entrega = entregaRepository.findById(idEntrega)
        if (entrega == null) {
            _state.value = _state.value?.copy(
                cargando = false,
                errorCarga = "No se encontro la entrega indicada en el acta."
            )
            return
        }

        if (entrega.estado != EstadoEntrega.CONFIRMADA) {
            _state.value = _state.value?.copy(
                cargando = false,
                errorGeneral = "Solo se puede generar el acta de entregas confirmadas."
            )
            return
        }

        val objeto = objetoRepository.findById(entrega.objetoEncontradoId)
        val persona = personaRepository.findById(entrega.receptorId)
        val administrador = usuarioRepository.findById(entrega.administradorId)

        val eventoAuditoria = EventoAuditoria(
            id = "aud-${AlmacenLocal.instancia.eventosAuditoria.size + 1}",
            entidadTipo = "Entrega",
            entidadId = entrega.id,
            accion = "ACTA_CONSULTADA",
            usuarioId = actorId,
            fecha = entrega.fechaConfirmacion ?: fechaActual(),
            detalle = "Acta de entrega consultada para la entrega ${entrega.id}",
            estadoAnterior = entrega.estado.name,
            estadoNuevo = entrega.estado.name
        )
        auditoriaRepository.save(eventoAuditoria)

        _state.value = _state.value?.copy(
            cargando = false,
            data = ActaEntrega(entrega, objeto, persona, administrador),
            errorCarga = null,
            errorGeneral = null
        )
    }

    companion object {
        fun etiquetaEstadoActa(entrega: Entrega?): String =
            when (entrega?.estado) {
                EstadoEntrega.CONFIRMADA -> "CERRADA"
                EstadoEntrega.RESERVADA -> "ABIERTA"
                EstadoEntrega.INCIERTA -> "INCIERTA"
                EstadoEntrega.CANCELADA -> "CANCELADA"
                null -> ""
            }

        fun correlativoActa(entrega: Entrega): String =
            "ACTA-UTP-2026-" + entrega.id.substringAfterLast("-")

        fun formatearFechaCierre(fechaConfirmacion: String?): String {
            if (fechaConfirmacion.isNullOrBlank()) return "—"
            return try {
                val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("es", "ES"))
                val fecha = formato.parse(fechaConfirmacion) ?: return fechaConfirmacion
                SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale("es", "ES")).format(fecha)
            } catch (e: Exception) {
                fechaConfirmacion
            }
        }

        fun fechaActual(): String =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("es", "ES")).format(Date())
    }

    class Factory(
        private val entregaRepository: EntregaRepository,
        private val usuarioRepository: UsuarioRepository,
        private val objetoRepository: ObjetoEncontradoRepository,
        private val personaRepository: PersonaAtendidaRepository,
        private val auditoriaRepository: AuditoriaRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(ActaEntregaViewModel::class.java)) {
                throw IllegalArgumentException("Clase no valida: ${modelClass.name}")
            }
            @Suppress("UNCHECKED_CAST")
            return ActaEntregaViewModel(
                entregaRepository = entregaRepository,
                usuarioRepository = usuarioRepository,
                objetoRepository = objetoRepository,
                personaRepository = personaRepository,
                auditoriaRepository = auditoriaRepository,
                actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
                actorRol = LoginRole.ADMINISTRADOR_SAE
            ) as T
        }
    }
}
