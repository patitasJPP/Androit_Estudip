package com.Jair.practica.viewmodel.perdida

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.entity.PersonaAtendida
import com.Jair.practica.model.entity.Reclamacion
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.model.local.LocalPersonaAtendidaRepository
import com.Jair.practica.model.local.LocalReclamacionRepository
import com.Jair.practica.model.local.LocalReportePerdidaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.remote.SesionRemota
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReportePerdidaViewModel : ViewModel() {
    private val reportes = LocalReportePerdidaRepository()
    private val objetos = LocalObjetoEncontradoRepository()
    private val reclamaciones = LocalReclamacionRepository()
    private val personas = LocalPersonaAtendidaRepository()
    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje
    private val _reporte = MutableLiveData<ReportePerdida>()
    val reporte: LiveData<ReportePerdida> = _reporte
    private val _coincidencias = MutableLiveData<List<ObjetoEncontrado>>()
    val coincidencias: LiveData<List<ObjetoEncontrado>> = _coincidencias

    fun registrar(rol: LoginRole, usuarioId: String, descripcion: String, lugar: String, contacto: String) {
        if (rol != LoginRole.PERSONAL_REGISTRADOR && rol != LoginRole.ADMINISTRADOR_SAE) {
            _mensaje.value = "El rol no está autorizado para registrar una pérdida."
            return
        }
        if (descripcion.isBlank() || contacto.isBlank()) {
            _mensaje.value = "Complete la descripción y el medio de contacto."
            return
        }
        val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        val guardado = reportes.save(ReportePerdida("", usuarioId, descripcion.trim(), lugar.trim(), fecha,
            EstadoReportePerdida.BUSCANDO, usuarioId, contacto.trim(), SesionRemota.usuarioId))
        _reporte.value = guardado
    }

    fun buscar(reporte: ReportePerdida) {
        val tokens = reporte.descripcion.lowercase(Locale.getDefault()).split(" ").filter { it.length >= 3 }
        val resultados = objetos.list().filter { objeto ->
            objeto.estado == com.Jair.practica.model.entity.EstadoObjeto.BAJO_CUSTODIA &&
                (tokens.isEmpty() || tokens.any { token -> objeto.descripcion.lowercase().contains(token) || objeto.categoria.lowercase().contains(token) }) &&
                CoincidenciaMatcher.coincideLugar(reporte.lugarAproximado, objeto.lugarHallazgo)
        }
        _coincidencias.value = resultados
    }

    /** Una sugerencia no es una reclamación: esta operación exige elegir un objeto concreto. */
    fun tieneSolicitud(reporte: ReportePerdida, objetoId: String): Boolean {
        val contacto = reporte.contacto?.trim()?.lowercase(Locale.ROOT)
        val store = AlmacenLocal.instancia
        return store.reclamaciones.any { reclamacion ->
            if (reclamacion.objetoEncontradoId != objetoId ||
                reclamacion.estado == EstadoReclamacion.RECHAZADA) return@any false
            if (reclamacion.reportePerdidaId == reporte.id) return@any true
            if (contacto.isNullOrBlank()) return@any false
            store.reportesPerdida.firstOrNull { it.id == reclamacion.reportePerdidaId }
                ?.contacto?.trim()?.lowercase(Locale.ROOT) == contacto
        }
    }

    fun solicitarVerificacion(reporte: ReportePerdida, objetoId: String): String {
        val objeto = objetos.list().firstOrNull { it.id == objetoId }
            ?: return "El objeto ya no está disponible. Actualiza la búsqueda."
        if (objeto.estado != com.Jair.practica.model.entity.EstadoObjeto.BAJO_CUSTODIA) {
            return "El objeto debe estar bajo custodia del SAE."
        }
        if (_coincidencias.value.orEmpty().none { it.id == objetoId }) {
            return "Actualiza la búsqueda antes de solicitar la verificación."
        }
        if (tieneSolicitud(reporte, objetoId)) {
            return "Este contacto ya tiene una solicitud de verificación para el objeto."
        }

        val personaId = "per-${reporte.id}"
        personas.save(PersonaAtendida(
            id = personaId,
            nombre = "Solicitante del reporte",
            vinculoUniversidad = "Persona atendida por SAE",
            identificador = reporte.creadoPor,
            correo = reporte.contacto
        ))
        val reclamacion = Reclamacion(
            id = "rec-${UUID.randomUUID()}",
            objetoEncontradoId = objetoId,
            personaAtendidaId = personaId,
            reportePerdidaId = reporte.id,
            estado = com.Jair.practica.model.entity.EstadoReclamacion.PENDIENTE_VERIFICACION,
            fechaRegistro = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date()),
            descripcionReclamante = reporte.descripcion,
            caracteristicasDeclaradas = reporte.descripcion
        )
        reclamaciones.save(reclamacion)
        AlmacenLocal.instancia.ultimaReclamacionPendienteId = reclamacion.id
        return "Solicitud registrada para ${objeto.codigoCustodia}. El SAE verificará la propiedad."
    }
}
