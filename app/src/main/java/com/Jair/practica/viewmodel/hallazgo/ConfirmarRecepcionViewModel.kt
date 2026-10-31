package com.Jair.practica.viewmodel.hallazgo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.UbicacionAlmacenamiento
import com.Jair.practica.model.local.ObjetoEncontradoLocalImpl
import com.Jair.practica.model.local.UbicacionLocalImpl
import com.Jair.practica.model.repository.ObjetoEncontradoRepository
import com.Jair.practica.model.repository.UbicacionRepository
import java.util.UUID

class ConfirmarRecepcionViewModel : ViewModel() {

    // Repositorios locales
    private val objetoRepository: ObjetoEncontradoRepository = ObjetoEncontradoLocalImpl()
    private val ubicacionRepository: UbicacionRepository = UbicacionLocalImpl()

    private val _ubicaciones = MutableLiveData<List<UbicacionAlmacenamiento>>()
    val ubicaciones: LiveData<List<UbicacionAlmacenamiento>> get() = _ubicaciones

    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> get() = _mensajeError

    private val _confirmacionExitosa = MutableLiveData<ObjetoEncontrado>()
    val confirmacionExitosa: LiveData<ObjetoEncontrado> get() = _confirmacionExitosa

    fun cargarUbicaciones() {
        try {
            _ubicaciones.value = ubicacionRepository.listActive()
        } catch (e: Exception) {
            _mensajeError.value = "Error al cargar ubicaciones: ${e.message}"
        }
    }

    fun confirmarCustodia(rolActivo: LoginRole, idObjeto: String, idUbicacion: String) {
        // Control de rol: Solo el Administrador SAE autoriza la custodia
        if (rolActivo != LoginRole.ADMINISTRADOR_SAE) {
            _mensajeError.value = "Operación denegada. Solo el Administrador SAE puede confirmar la custodia."
            return
        }

        // Control de entrada
        if (idObjeto.isBlank() || idUbicacion.isBlank()) {
            _mensajeError.value = "Falta código de objeto o no ha seleccionado una ubicación válida."
            return
        }

        try {
            val objetoActual = objetoRepository.findById(idObjeto)
            if (objetoActual == null) {
                _mensajeError.value = "El objeto especificado no existe en el sistema."
                return
            }

            // Generación de código de custodia y asignación de estado (RF-03)
            val codigoGenerado = "CUST-${UUID.randomUUID().toString().substring(0, 6).uppercase()}"

            val objetoActualizado = objetoActual.copy(
                codigoCustodia = codigoGenerado,
                ubicacionId = idUbicacion,
                estado = EstadoObjeto.BAJO_CUSTODIA
            )

            val resultado = objetoRepository.update(objetoActualizado)
            _confirmacionExitosa.value = resultado

        } catch (e: Exception) {
            _mensajeError.value = "Error en la confirmación de custodia: ${e.message}"
        }
    }
}