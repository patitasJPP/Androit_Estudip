package com.Jair.practica

import android.content.Context
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.entity.Reclamacion
import com.Jair.practica.model.entity.Entrega
import com.Jair.practica.model.entity.PersonaAtendida
import com.Jair.practica.model.entity.EstadoEntrega
import com.Jair.practica.model.entity.EstadoReclamacion
import org.json.JSONArray
import org.json.JSONObject

object PersistenciaLocal {
    private const val PREFS = "utp_objects_data"
    private const val OBJETOS = "objetos"
    private const val REPORTES = "reportes"
    private const val RECLAMACIONES = "reclamaciones"
    private const val ENTREGAS = "entregas"
    private const val PERSONAS = "personas"
    private var prefs: android.content.SharedPreferences? = null

    fun inicializar(context: Context) { prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    fun guardarObjetos(objetos: List<ObjetoEncontrado>) {
        val array = JSONArray()
        objetos.filter { it.id.startsWith("hallazgo-") || it.id.startsWith("obj-local-") || it.id.length > 20 }.forEach { objeto ->
            array.put(JSONObject().apply {
                put("id", objeto.id); put("codigo", objeto.codigoCustodia); put("categoria", objeto.categoria); put("descripcion", objeto.descripcion)
                put("reservadas", objeto.caracteristicasReservadas ?: ""); put("lugar", objeto.lugarHallazgo ?: ""); put("fecha", objeto.fechaHallazgo ?: "")
                put("registradoPor", objeto.registradoPor); put("estado", objeto.estado.name); put("ubicacion", objeto.ubicacionId ?: ""); put("contacto", objeto.contactoHalladorId ?: "")
            })
        }
        prefs?.edit()?.putString(OBJETOS, array.toString())?.apply()
    }

    fun cargarObjetos(): List<ObjetoEncontrado> {
        val raw = prefs?.getString(OBJETOS, null) ?: return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            ObjetoEncontrado(o.getString("id"), o.optString("codigo"), o.optString("categoria"), o.optString("descripcion"), o.optString("reservadas").ifBlank { null }, o.optString("lugar").ifBlank { null }, o.optString("fecha").ifBlank { null }, o.optString("registradoPor"), runCatching { EstadoObjeto.valueOf(o.optString("estado")) }.getOrDefault(EstadoObjeto.HALLAZGO_REPORTADO), o.optString("ubicacion").ifBlank { null }, o.optString("contacto").ifBlank { null })
        }
    }

    fun guardarReportes(reportes: List<ReportePerdida>) {
        val array = JSONArray()
        reportes.filter { it.id.length > 20 }.forEach { reporte ->
            array.put(JSONObject().apply { put("id", reporte.id); put("persona", reporte.personaAtendidaId); put("descripcion", reporte.descripcion); put("lugar", reporte.lugarAproximado ?: ""); put("fecha", reporte.fechaReporte); put("estado", reporte.estado.name); put("creadoPor", reporte.creadoPor); put("contacto", reporte.contacto ?: ""); put("registradoPorRemoto", reporte.registradoPorRemoto ?: "") })
        }
        prefs?.edit()?.putString(REPORTES, array.toString())?.apply()
    }

    fun cargarReportes(): List<ReportePerdida> {
        val raw = prefs?.getString(REPORTES, null) ?: return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            ReportePerdida(o.getString("id"), o.optString("persona"), o.optString("descripcion"), o.optString("lugar").ifBlank { null }, o.optString("fecha"), runCatching { EstadoReportePerdida.valueOf(o.optString("estado")) }.getOrDefault(EstadoReportePerdida.BUSCANDO), o.optString("creadoPor"), o.optString("contacto").ifBlank { null }, o.optString("registradoPorRemoto").ifBlank { null })
        }
    }

    fun guardarReclamaciones(reclamaciones: List<Reclamacion>) {
        val array = JSONArray()
        reclamaciones.filter { it.id.length > 20 }.forEach { r ->
            array.put(JSONObject().apply { put("id", r.id); put("objeto", r.objetoEncontradoId); put("persona", r.personaAtendidaId); put("reporte", r.reportePerdidaId ?: ""); put("estado", r.estado.name); put("motivo", r.motivoDecision ?: ""); put("revisado", r.revisadoPor ?: ""); put("fecha", r.fechaRegistro ?: ""); put("descripcion", r.descripcionReclamante ?: ""); put("caracteristicas", r.caracteristicasDeclaradas ?: "") })
        }
        prefs?.edit()?.putString(RECLAMACIONES, array.toString())?.apply()
    }

    fun cargarReclamaciones(): List<Reclamacion> {
        val raw = prefs?.getString(RECLAMACIONES, null) ?: return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            Reclamacion(o.getString("id"), o.optString("objeto"), o.optString("persona"), o.optString("reporte").ifBlank { null }, estado = runCatching { EstadoReclamacion.valueOf(o.optString("estado")) }.getOrDefault(EstadoReclamacion.PENDIENTE_VERIFICACION), motivoDecision = o.optString("motivo").ifBlank { null }, revisadoPor = o.optString("revisado").ifBlank { null }, fechaRegistro = o.optString("fecha").ifBlank { null }, descripcionReclamante = o.optString("descripcion").ifBlank { null }, caracteristicasDeclaradas = o.optString("caracteristicas").ifBlank { null })
        }
    }

    fun guardarEntregas(entregas: List<Entrega>) {
        val array = JSONArray()
        entregas.filter { it.id.length > 20 || (it.reclamacionId.startsWith("rec-") && it.reclamacionId.length > 20) }.forEach { e ->
            array.put(JSONObject().apply { put("id", e.id); put("objeto", e.objetoEncontradoId); put("reclamacion", e.reclamacionId); put("receptor", e.receptorId); put("admin", e.administradorId); put("fecha", e.fechaConfirmacion ?: ""); put("firma", e.firmaCapturada); put("estado", e.estado.name); put("dni", e.receptorDni ?: ""); put("correo", e.receptorCorreo ?: ""); put("operacion", e.idOperacion ?: ""); put("firmaReferencia", e.firmaReferencia ?: "") })
        }
        prefs?.edit()?.putString(ENTREGAS, array.toString())?.apply()
    }

    fun cargarEntregas(): List<Entrega> {
        val raw = prefs?.getString(ENTREGAS, null) ?: return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            Entrega(o.getString("id"), o.optString("objeto"), o.optString("reclamacion"), o.optString("receptor"), o.optString("admin"), o.optString("fecha").ifBlank { null }, o.optBoolean("firma"), runCatching { EstadoEntrega.valueOf(o.optString("estado")) }.getOrDefault(EstadoEntrega.RESERVADA), idOperacion = o.optString("operacion").ifBlank { null }, firmaReferencia = o.optString("firmaReferencia").ifBlank { null }, receptorDni = o.optString("dni").ifBlank { null }, receptorCorreo = o.optString("correo").ifBlank { null })
        }
    }

    fun guardarPersonas(personas: List<PersonaAtendida>) {
        val array = JSONArray()
        personas.filter { it.id.length > 20 }.forEach { persona ->
            array.put(JSONObject().apply {
                put("id", persona.id); put("nombre", persona.nombre); put("vinculo", persona.vinculoUniversidad)
                put("identificador", persona.identificador ?: ""); put("dni", persona.dni ?: "")
                put("correo", persona.correo ?: ""); put("telefono", persona.telefono ?: "")
            })
        }
        prefs?.edit()?.putString(PERSONAS, array.toString())?.apply()
    }

    fun cargarPersonas(): List<PersonaAtendida> {
        val raw = prefs?.getString(PERSONAS, null) ?: return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            PersonaAtendida(o.getString("id"), o.optString("nombre"), o.optString("vinculo"),
                o.optString("identificador").ifBlank { null }, o.optString("dni").ifBlank { null },
                o.optString("correo").ifBlank { null }, o.optString("telefono").ifBlank { null })
        }
    }
}
