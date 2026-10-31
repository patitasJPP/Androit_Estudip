package com.Jair.practica.viewmodel.devolucion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Jair.practica.model.entity.Entrega
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.PersonaAtendida
import com.Jair.practica.model.entity.Reclamacion
import com.Jair.practica.model.local.LocalAuditoriaRepository
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.LocalEntregaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.LocalPersonaAtendidaRepository
import com.Jair.practica.model.local.LocalReclamacionRepository
import com.Jair.practica.model.repository.AuditoriaRepository
import com.Jair.practica.model.repository.EntregaRepository
import com.Jair.practica.model.repository.ObjetoEncontradoRepository
import com.Jair.practica.model.repository.PersonaAtendidaRepository
import com.Jair.practica.model.repository.ReclamacionRepository
import com.Jair.practica.model.repository.ResultadoEntrega
import android.graphics.Bitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ConfirmarDevolucionUiState(
    val cargando: Boolean = true,
    val errorAcceso: Boolean = false,
    val errorCarga: String? = null,
    val reclamacion: Reclamacion? = null,
    val objeto: ObjetoEncontrado? = null,
    val persona: PersonaAtendida? = null,
    val entrega: Entrega? = null,
    val decision: EstadoReclamacion? = null,
    val receptorId: String = "",
    val dni: String = "",
    val correo: String = "",
    val errorReceptor: Boolean = false,
    val errorDni: Boolean = false,
    val errorCorreo: Boolean = false,
    val firmaCapturada: Boolean = false,
    val errorFirma: Boolean = false,
    val enviando: Boolean = false,
    val bloqueada: Boolean = false,
    val mensajeExito: String? = null,
    val errorGeneral: String? = null
)

class ConfirmarDevolucionViewModel(
    private val reclamacionRepository: ReclamacionRepository = LocalReclamacionRepository(),
    private val objetoRepository: ObjetoEncontradoRepository = LocalObjetoEncontradoRepository(),
    private val personaRepository: PersonaAtendidaRepository = LocalPersonaAtendidaRepository(),
    private val entregaRepository: EntregaRepository = LocalEntregaRepository(),
    private val auditoriaRepository: AuditoriaRepository = LocalAuditoriaRepository(),
    private val actorId: String = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
    private val actorRol: LoginRole = LoginRole.ADMINISTRADOR_SAE
) : ViewModel() {

    private val _state = MutableLiveData<ConfirmarDevolucionUiState>(ConfirmarDevolucionUiState())
    val state: LiveData<ConfirmarDevolucionUiState> = _state

    private var idOperacionActual: String? = UUID.randomUUID().toString()
    private var firmaBitmap: Bitmap? = null

    fun setFirmaBitmap(bitmap: Bitmap?) {
        firmaBitmap = bitmap
    }

    fun cargar(reclamacionId: String) {
        val estadoActual = _state.value
        _state.value = estadoActual?.copy(
            cargando = true,
            errorCarga = null,
            reclamacion = null,
            objeto = null,
            persona = null,
            entrega = null,
            decision = null,
            receptorId = "",
            dni = "",
            correo = "",
            errorReceptor = false,
            errorDni = false,
            errorCorreo = false,
            firmaCapturada = false,
            errorFirma = false,
            enviando = false,
            bloqueada = false,
            mensajeExito = null,
            errorGeneral = null
        )

        if (actorRol != LoginRole.ADMINISTRADOR_SAE) {
            _state.value = _state.value?.copy(errorAcceso = true, cargando = false)
            return
        }

        val reclamacion = reclamacionRepository.findByIdForActor(reclamacionId, actorRol)
        if (reclamacion == null) {
            _state.value = _state.value?.copy(cargando = false, errorCarga = "No se encontró la reclamación a confirmar.")
            return
        }

        val objeto = objetoRepository.findById(reclamacion.objetoEncontradoId)
        val persona = personaRepository.findById(reclamacion.personaAtendidaId)
        val bloqueada = reclamacion.estado != EstadoReclamacion.APROBADA ||
            objeto?.estado == EstadoObjeto.ENTREGADO

        _state.value = _state.value?.copy(
            cargando = false,
            reclamacion = reclamacion,
            objeto = objeto,
            persona = persona,
            decision = reclamacion.estado,
            receptorId = persona?.id ?: "",
            dni = persona?.dni ?: "",
            correo = persona?.correo ?: "",
            bloqueada = bloqueada
        )
    }

    fun onFirmaChange(capturada: Boolean) {
        _state.value = _state.value?.copy(firmaCapturada = capturada, errorFirma = false)
    }

    fun onReceptorChange(receptorId: String) {
        _state.value = _state.value?.copy(receptorId = receptorId, errorReceptor = false)
    }

    fun onDniChange(dni: String) {
        _state.value = _state.value?.copy(dni = dni.trim(), errorDni = false)
    }

    fun onCorreoChange(correo: String) {
        _state.value = _state.value?.copy(correo = correo.trim(), errorCorreo = false)
    }

    fun confirmar(): Boolean {
        val estadoActual = _state.value ?: return false
        if (estadoActual.enviando || estadoActual.bloqueada || estadoActual.errorAcceso) {
            return false
        }
        if (estadoActual.reclamacion?.estado != EstadoReclamacion.APROBADA) {
            _state.value = estadoActual.copy(
                enviando = false,
                errorGeneral = "La reclamación debe estar en estado Aprobada para confirmar la devolución."
            )
            return false
        }
        if (estadoActual.objeto?.estado == EstadoObjeto.ENTREGADO) {
            _state.value = estadoActual.copy(
                enviando = false,
                bloqueada = true,
                errorGeneral = "El objeto ya fue entregado y no puede confirmarse otra entrega."
            )
            return false
        }
        if (!estadoActual.firmaCapturada) {
            _state.value = estadoActual.copy(enviando = false, errorFirma = true)
            return false
        }
        if (estadoActual.receptorId.isBlank() || !esDniValido(estadoActual.dni) || !esCorreoValido(estadoActual.correo)) {
            _state.value = estadoActual.copy(
                enviando = false,
                errorFirma = false,
                errorReceptor = estadoActual.receptorId.isBlank(),
                errorDni = !esDniValido(estadoActual.dni),
                errorCorreo = !esCorreoValido(estadoActual.correo)
            )
            return false;
        }

        _state.value = estadoActual.copy(enviando = true, errorGeneral = null)

        val resultado = entregaRepository.confirmarEntrega(
            reclamacionId = estadoActual.reclamacion?.id ?: return false,
            receptorId = estadoActual.receptorId,
            administradorId = actorId,
            firmaCapturada = true,
            fecha = fechaActual(),
            idOperacion = idOperacionActual ?: ""
        )

        return when (resultado) {
            is ResultadoEntrega.Denegada -> {
                _state.value = _state.value?.copy(enviando = false, errorGeneral = "La confirmación de devolución fue rechazada: ${resultado.motivo}.")
                false
            }
            is ResultadoEntrega.Registrada -> {
                idOperacionActual = null
                val entregaConFirma = resultado.entrega.copy(firmaReferencia = if (firmaBitmap != null) "firma-${resultado.entrega.id}" else null)
                entregaRepository.update(entregaConFirma)
                _state.value = _state.value?.copy(
                    enviando = false,
                    entrega = entregaConFirma,
                    mensajeExito = "Devolución confirmada. El objeto pasa a estado Entregado."
                )
                true
            }
        }
    }

    companion object {
        const val DNI_MIN = 8

        fun esDniValido(dni: String): Boolean = dni.length == DNI_MIN && dni.all { it.isDigit() }

        fun esCorreoValido(correo: String): Boolean =
            Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(correo)

        fun fechaActual(): String =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("es", "ES")).format(Date())
    }

    class Factory(
        private val reclamacionRepository: ReclamacionRepository,
        private val objetoRepository: ObjetoEncontradoRepository,
        private val personaRepository: PersonaAtendidaRepository,
        private val entregaRepository: EntregaRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(ConfirmarDevolucionViewModel::class.java)) {
                throw IllegalArgumentException("Clase no válida: ${modelClass.name}")
            }
            @Suppress("UNCHECKED_CAST")
            return ConfirmarDevolucionViewModel(
                reclamacionRepository = reclamacionRepository,
                objetoRepository = objetoRepository,
                personaRepository = personaRepository,
                entregaRepository = entregaRepository,
                actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
                actorRol = LoginRole.ADMINISTRADOR_SAE
            ) as T
        }
    }
}
