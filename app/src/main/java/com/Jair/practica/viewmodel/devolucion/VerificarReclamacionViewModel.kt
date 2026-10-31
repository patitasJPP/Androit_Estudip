package com.Jair.practica.viewmodel.devolucion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Jair.practica.model.entity.Evidencia
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.PersonaAtendida
import com.Jair.practica.model.entity.PreguntaVerificacion
import com.Jair.practica.model.entity.Reclamacion
import com.Jair.practica.model.local.LocalAuditoriaRepository
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.LocalEvidenciaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.LocalPersonaAtendidaRepository
import com.Jair.practica.model.local.LocalReclamacionRepository
import com.Jair.practica.model.repository.AuditoriaRepository
import com.Jair.practica.model.repository.EvidenciaRepository
import com.Jair.practica.model.repository.ObjetoEncontradoRepository
import com.Jair.practica.model.repository.PersonaAtendidaRepository
import com.Jair.practica.model.repository.ReclamacionRepository
import com.Jair.practica.model.repository.ResultadoDecision
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CampoComparacion(
    val etiqueta: String,
    val valorReclamante: String,
    val valorRegistrado: String
)

data class PreguntaFormulario(
    val id: String,
    val texto: String,
    val respuesta: String = ""
)

data class VerificarReclamacionUiState(
    val cargando: Boolean = true,
    val errorAcceso: Boolean = false,
    val errorCarga: String? = null,
    val reclamacion: Reclamacion? = null,
    val persona: PersonaAtendida? = null,
    val objeto: ObjetoEncontrado? = null,
    val evidencias: List<Evidencia> = emptyList(),
    val comparacion: List<CampoComparacion> = emptyList(),
    val decision: EstadoReclamacion? = null,
    val decisionSeleccionada: Boolean = false,
    val motivo: String = "",
    val errorDecision: Boolean = false,
    val errorMotivo: Boolean = false,
    val preguntas: List<PreguntaFormulario> = emptyList(),
    val errorPregunta: Boolean = false,
    val enviando: Boolean = false,
    val bloqueada: Boolean = false,
    val mostrarCaracteristicasReservadas: Boolean = false,
    val mensajeExito: String? = null,
    val errorGeneral: String? = null
)

class VerificarReclamacionViewModel(
    private val reclamacionRepository: ReclamacionRepository = LocalReclamacionRepository(),
    private val evidenciaRepository: EvidenciaRepository = LocalEvidenciaRepository(),
    private val auditoriaRepository: AuditoriaRepository = LocalAuditoriaRepository(),
    private val objetoRepository: ObjetoEncontradoRepository = LocalObjetoEncontradoRepository(),
    private val personaRepository: PersonaAtendidaRepository = LocalPersonaAtendidaRepository(),
    private val actorId: String = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
    private val actorRol: LoginRole = LoginRole.ADMINISTRADOR_SAE
) : ViewModel() {

    private val _state = MutableLiveData<VerificarReclamacionUiState>(VerificarReclamacionUiState())
    val state: LiveData<VerificarReclamacionUiState> = _state

    private var idOperacionActual: String? = UUID.randomUUID().toString()

    fun cargar(reclamacionId: String) {
        val estadoActual = _state.value
        _state.value = estadoActual?.copy(
            cargando = true,
            errorCarga = null,
            reclamacion = null,
            persona = null,
            objeto = null,
            evidencias = emptyList(),
            comparacion = emptyList(),
            decision = null,
            decisionSeleccionada = false,
            motivo = "",
            errorDecision = false,
            errorMotivo = false,
            preguntas = emptyList(),
            errorPregunta = false,
            enviando = false,
            bloqueada = false,
            mensajeExito = null,
            errorGeneral = null,
            mostrarCaracteristicasReservadas = actorRol == LoginRole.ADMINISTRADOR_SAE
        )

        if (actorRol != LoginRole.ADMINISTRADOR_SAE) {
            _state.value = _state.value?.copy(errorAcceso = true, cargando = false)
            return
        }

        val reclamacion = reclamacionRepository.findByIdForActor(reclamacionId, actorRol)
        if (reclamacion == null) {
            _state.value = _state.value?.copy(cargando = false, errorCarga = "No se encontró la reclamación indicada.")
            return
        }

        val persona = personaRepository.findById(reclamacion.personaAtendidaId)
        val objeto = objetoRepository.findById(reclamacion.objetoEncontradoId)
        val evidencias = evidenciaRepository.listByReclamacion(reclamacion.id, actorRol)
        val bloqueada = reclamacion.estado != EstadoReclamacion.PENDIENTE_VERIFICACION ||
            objeto?.estado == EstadoObjeto.ENTREGADO

        _state.value = _state.value?.copy(
            cargando = false,
            reclamacion = reclamacion,
            persona = persona,
            objeto = objeto,
            evidencias = evidencias,
            bloqueada = bloqueada,
            mostrarCaracteristicasReservadas = actorRol == LoginRole.ADMINISTRADOR_SAE,
            comparacion = listOf(
                CampoComparacion(
                    etiqueta = "Descripción del reclamante",
                    valorReclamante = reclamacion.descripcionReclamante ?: "",
                    valorRegistrado = objeto?.descripcion ?: ""
                ),
                CampoComparacion(
                    etiqueta = "Características declaradas",
                    valorReclamante = reclamacion.caracteristicasDeclaradas ?: "",
                    valorRegistrado = objeto?.caracteristicasReservadas ?: ""
                )
            )
        )
    }

    fun seleccionarDecision(estado: EstadoReclamacion?) {
        val estadoActual = _state.value ?: return
        _state.value = estadoActual.copy(
            decision = estado,
            decisionSeleccionada = estado != null,
            errorDecision = false,
            errorGeneral = null
        )
    }

    fun onMotivoChange(motivo: String) {
        val estadoActual = _state.value ?: return
        val motivoLimpio = motivo.trim()
        _state.value = estadoActual.copy(
            motivo = motivoLimpio,
            errorMotivo = motivoLimpio.isNotEmpty() && (motivoLimpio.length < MOTIVO_MIN || motivoLimpio.length > MOTIVO_MAX)
        )
    }

    fun agregarPregunta(texto: String): Boolean {
        val estadoActual = _state.value ?: return false
        val preguntaLimpia = texto.trim()
        if (preguntaLimpia.length < PREGUNTA_MIN || preguntaLimpia.length > PREGUNTA_MAX) {
            _state.value = estadoActual.copy(errorPregunta = true)
            return false
        }
        val nuevaPregunta = PreguntaFormulario(id = UUID.randomUUID().toString(), texto = preguntaLimpia)
        _state.value = estadoActual.copy(
            preguntas = estadoActual.preguntas + nuevaPregunta,
            errorPregunta = false
        )
        return true
    }

    fun onRespuestaChange(id: String, respuesta: String) {
        val estadoActual = _state.value ?: return
        _state.value = estadoActual.copy(
            preguntas = estadoActual.preguntas.map {
                if (it.id == id) it.copy(respuesta = respuesta) else it
            }
        )
    }

    fun registrarDecision() {
        val estadoActual = _state.value ?: return
        if (estadoActual.enviando || estadoActual.bloqueada || estadoActual.errorAcceso) {
            return
        }
        if (!estadoActual.decisionSeleccionada || estadoActual.decision == null) {
            _state.value = estadoActual.copy(errorDecision = true)
            return
        }
        val motivoLimpio = estadoActual.motivo.trim()
        if (motivoLimpio.length < MOTIVO_MIN || motivoLimpio.length > MOTIVO_MAX) {
            _state.value = estadoActual.copy(errorDecision = false, errorMotivo = true)
            return
        }
        if (estadoActual.preguntas.isEmpty()) {
            _state.value = estadoActual.copy(errorDecision = false, errorMotivo = false, errorPregunta = true)
            return
        }
        if (estadoActual.preguntas.any { it.texto.trim().length < PREGUNTA_MIN || it.texto.trim().length > PREGUNTA_MAX }) {
            _state.value = estadoActual.copy(errorDecision = false, errorMotivo = false, errorPregunta = true)
            return
        }

        _state.value = estadoActual.copy(
            enviando = true,
            errorDecision = false,
            errorMotivo = false,
            errorPregunta = false,
            errorGeneral = null
        )

        val decision = estadoActual.decision
        val reclamacionId = estadoActual.reclamacion?.id
        if (reclamacionId == null || decision == null) {
            _state.value = _state.value?.copy(enviando = false)
            return
        }

        val resultado = reclamacionRepository.registrarDecision(
            reclamacionId = reclamacionId,
            nuevoEstado = decision,
            motivo = motivoLimpio,
            preguntas = estadoActual.preguntas.map {
                PreguntaVerificacion(
                    id = it.id,
                    pregunta = it.texto.trim(),
                    respuesta = it.respuesta.takeIf { respuesta -> respuesta.isNotBlank() }
                )
            },
            actorId = actorId,
            actorRol = actorRol,
            fecha = fechaActual(),
            idOperacion = idOperacionActual ?: ""
        )

        when (resultado) {
            is ResultadoDecision.Denegada -> {
                _state.value = _state.value?.copy(enviando = false, errorGeneral = "La decisión no pudo registrarse: ${resultado.motivo.name}")
            }
            is ResultadoDecision.Registrada -> {
                val actualizada = resultado.reclamacion
                idOperacionActual = null
                _state.value = _state.value?.copy(
                    enviando = false,
                    reclamacion = actualizada,
                    decision = actualizada.estado,
                    motivo = actualizada.motivoDecision ?: "",
                    preguntas = actualizada.preguntas.map {
                        PreguntaFormulario(id = it.id, texto = it.pregunta, respuesta = it.respuesta ?: "")
                    },
                    bloqueada = actualizada.estado != EstadoReclamacion.PENDIENTE_VERIFICACION,
                    mensajeExito = "Decisión registrada: ${etiquetaEstado(actualizada.estado)}"
                )
            }
        }
    }

    fun limpiarMensaje() {
        _state.value = _state.value?.copy(mensajeExito = null)
    }

    companion object {
        const val MOTIVO_MIN = 10
        const val MOTIVO_MAX = 300
        const val PREGUNTA_MIN = 10
        const val PREGUNTA_MAX = 200

        fun etiquetaEstado(estado: EstadoReclamacion?): String = when (estado) {
            EstadoReclamacion.PENDIENTE_VERIFICACION -> "Pendiente de verificación"
            EstadoReclamacion.APROBADA -> "Aprobada"
            EstadoReclamacion.RECHAZADA -> "Rechazada"
            EstadoReclamacion.ATENDIDA -> "Atendida"
            null -> ""
        }

        fun fechaActual(): String =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("es", "ES")).format(Date())
    }

    class Factory(
        private val reclamacionRepository: ReclamacionRepository,
        private val evidenciaRepository: EvidenciaRepository,
        private val auditoriaRepository: AuditoriaRepository,
        private val objetoRepository: ObjetoEncontradoRepository,
        private val personaRepository: PersonaAtendidaRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(VerificarReclamacionViewModel::class.java)) {
                throw IllegalArgumentException("Clase no válida: ${modelClass.name}")
            }
            @Suppress("UNCHECKED_CAST")
            return VerificarReclamacionViewModel(
                reclamacionRepository = reclamacionRepository,
                evidenciaRepository = evidenciaRepository,
                auditoriaRepository = auditoriaRepository,
                objetoRepository = objetoRepository,
                personaRepository = personaRepository,
                actorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
                actorRol = LoginRole.ADMINISTRADOR_SAE
            ) as T
        }
    }
}
