package com.Jair.practica.viewmodel.hallazgo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.local.ObjetoEncontradoLocalImpl
import com.Jair.practica.model.repository.ObjetoEncontradoRepository

class RegistroHallazgoViewModel : ViewModel() {

    // Instancia del repositorio local (en integración final, el Integrante 1 podría usar inyección de dependencias)
    private val repository: ObjetoEncontradoRepository = ObjetoEncontradoLocalImpl()

    // LiveData para notificar a la vista sobre errores de validación
    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> get() = _mensajeError

    // LiveData para notificar a la vista que el registro fue exitoso
    private val _registroExitoso = MutableLiveData<ObjetoEncontrado?>()
    val registroExitoso: LiveData<ObjetoEncontrado?> get() = _registroExitoso

    fun documentarHallazgo(
        rolActivo: LoginRole,
        categoria: String,
        descripcion: String,
        lugar: String,
        fechaHora: String,
        contacto: String,
        tieneEvidencia: Boolean
    ) {
        // Control de rol: Solo el Personal Registrador puede documentar
        if (rolActivo != LoginRole.PERSONAL_REGISTRADOR) {
            _mensajeError.value = "Operación denegada. Solo el Personal Registrador puede documentar hallazgos."
            return
        }

        // Controles de entrada: Validar campos obligatorios vacíos
        if (descripcion.isBlank() || lugar.isBlank() || fechaHora.isBlank() || contacto.isBlank() || !tieneEvidencia) {
            _mensajeError.value = "Debe completar descripción, lugar, fecha, contacto y adjuntar evidencia."
            return
        }

        // Aplicación de Regla de Negocio: Se crea forzando el estado inicial "HALLAZGO_REPORTADO"
        val nuevoHallazgo = ObjetoEncontrado(
            id = "", // El ID se genera en ObjetoEncontradoLocalImpl si va vacío
            codigoCustodia = "", // No tiene código hasta que el SAE confirme recepción (RF-03)
            categoria = categoria,
            descripcion = descripcion,
            lugarHallazgo = lugar,
            fechaHallazgo = fechaHora,
            contactoHalladorId = contacto,
            registradoPor = "Usuario_Simulado", // Dato simulado por ahora
            estado = EstadoObjeto.HALLAZGO_REPORTADO
        )

        try {
            // Guardar en el repositorio local
            val objetoGuardado = repository.save(nuevoHallazgo)
            _registroExitoso.value = objetoGuardado
        } catch (e: Exception) {
            _mensajeError.value = "Ocurrió un error al guardar: ${e.message}"
        }
    }
}