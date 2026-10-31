package com.Jair.practica.viewmodel.perdida

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.local.LocalReportePerdidaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportePerdidaViewModel : ViewModel() {
    private val reportes = LocalReportePerdidaRepository()
    private val objetos = LocalObjetoEncontradoRepository()
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
            EstadoReportePerdida.BUSCANDO, usuarioId, contacto.trim()))
        _reporte.value = guardado
        buscar(guardado)
    }

    fun buscar(reporte: ReportePerdida) {
        val tokens = reporte.descripcion.lowercase(Locale.getDefault()).split(" ").filter { it.length >= 3 }
        val resultados = objetos.list().filter { objeto ->
            objeto.estado != com.Jair.practica.model.entity.EstadoObjeto.ENTREGADO &&
                (tokens.isEmpty() || tokens.any { token -> objeto.descripcion.lowercase().contains(token) || objeto.categoria.lowercase().contains(token) }) &&
                (reporte.lugarAproximado.isNullOrBlank() || objeto.lugarHallazgo.orEmpty().contains(reporte.lugarAproximado, true))
        }
        _coincidencias.value = resultados
    }
}
