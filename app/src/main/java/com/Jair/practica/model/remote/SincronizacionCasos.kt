package com.Jair.practica.model.remote

import android.util.Log
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.Reclamacion
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.PersonaAtendida
import com.Jair.practica.model.entity.Entrega
import com.Jair.practica.model.entity.EstadoEntrega
import com.Jair.practica.model.entity.PreguntaVerificacion
import com.Jair.practica.PersistenciaLocal
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.AlmacenLocal
import java.util.concurrent.Executors
import java.io.ByteArrayOutputStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject

/** Crea reportes y reclamaciones en orden, respetando las claves foráneas de Supabase. */
object SincronizacionCasos {
    private val executor = Executors.newSingleThreadExecutor()
    private val clienteArchivos = OkHttpClient()

    /** Recupera casos de Supabase para que ambos roles vean el mismo estado. */
    fun refrescarCasos(alTerminar: () -> Unit = {}) {
        if (SesionRemota.accessToken == null) return
        executor.execute {
            runCatching {
                val reportesRespuesta = SupabaseClient.casos.consultarReportes().execute()
                val reclamacionesRespuesta = SupabaseClient.casos.consultarReclamaciones().execute()
                val objetosRespuesta = SupabaseClient.api.consultarObjetos().execute()
                val entregasRespuesta = if (SesionRemota.rol == "ADMINISTRADOR_SAE")
                    SupabaseClient.casos.consultarEntregas().execute() else null
                check(reportesRespuesta.isSuccessful && reclamacionesRespuesta.isSuccessful && objetosRespuesta.isSuccessful) {
                    "No se pudieron actualizar los casos de Supabase"
                }
                check(entregasRespuesta == null || entregasRespuesta.isSuccessful) { "No se pudo actualizar el historial de entregas" }
                val store = AlmacenLocal.instancia
                val reportesRemotos = reportesRespuesta.body().orEmpty()
                val objetosRemotos = objetosRespuesta.body().orEmpty().associateBy { it.id }
                val reportesPorId = reportesRemotos.associateBy { it.id }
                val reclamacionesPorId = reclamacionesRespuesta.body().orEmpty().associateBy { it.id }
                reportesRemotos.forEach { remoto ->
                    val actual = store.reportesPerdida.firstOrNull { it.id == remoto.codigo_reporte }
                    val estado = runCatching { EstadoReportePerdida.valueOf(remoto.estado) }
                        .getOrDefault(EstadoReportePerdida.BUSCANDO)
                    val reporte = ReportePerdida(
                        id = remoto.codigo_reporte,
                        personaAtendidaId = actual?.personaAtendidaId ?: "per-${remoto.codigo_reporte}",
                        descripcion = remoto.descripcion,
                        lugarAproximado = remoto.lugar_aproximado,
                        fechaReporte = actual?.fechaReporte ?: "",
                        estado = estado,
                        creadoPor = actual?.creadoPor ?: LocalDataStore.USUARIO_REGISTRADOR_ID,
                        contacto = remoto.contacto,
                        registradoPorRemoto = remoto.registrado_por
                    )
                    store.reportesPerdida.removeAll { it.id == reporte.id }
                    store.reportesPerdida.add(reporte)
                }
                reclamacionesRespuesta.body().orEmpty().forEach { remota ->
                    val reporte = reportesPorId[remota.reporte_perdida_id] ?: return@forEach
                    val objetoCodigo = objetosRemotos[remota.objeto_encontrado_id]?.codigo_custodia ?: return@forEach
                    val objeto = store.objetos.firstOrNull { it.codigoCustodia == objetoCodigo } ?: return@forEach
                    val actual = store.reclamaciones.firstOrNull { it.id == remota.codigo_reclamacion }
                    val personaId = actual?.personaAtendidaId ?: "per-${reporte.codigo_reporte}"
                    val personaActual = store.personas.firstOrNull { it.id == personaId }
                    if (personaActual == null) store.personas.add(PersonaAtendida(
                        personaId, remota.solicitante, "Persona atendida por SAE", correo = reporte.contacto
                    ))
                    val estado = runCatching { EstadoReclamacion.valueOf(remota.decision) }
                        .getOrDefault(EstadoReclamacion.PENDIENTE_VERIFICACION)
                    val reclamacion = (actual ?: Reclamacion(
                        id = remota.codigo_reclamacion,
                        objetoEncontradoId = objeto.id,
                        personaAtendidaId = personaId,
                        reportePerdidaId = reporte.codigo_reporte
                    )).copy(estado = estado, motivoDecision = remota.motivo_decision,
                        caracteristicasDeclaradas = remota.evidencia_aportada,
                        preguntas = leerPreguntas(remota.preguntas_verificacion, actual?.preguntas.orEmpty()))
                    store.reclamaciones.removeAll { it.id == reclamacion.id }
                    store.reclamaciones.add(reclamacion)
                    if (estado == EstadoReclamacion.PENDIENTE_VERIFICACION)
                        store.ultimaReclamacionPendienteId = reclamacion.id
                }
                entregasRespuesta?.body().orEmpty().forEach { remota ->
                    val reclamacionCodigo = reclamacionesPorId[remota.reclamacion_id]?.codigo_reclamacion
                        ?: return@forEach
                    val reclamacion = store.reclamaciones.firstOrNull { it.id == reclamacionCodigo }
                        ?: return@forEach
                    val persona = store.personas.firstOrNull { it.id == reclamacion.personaAtendidaId }
                    if (persona != null) {
                        store.personas.removeAll { it.id == persona.id }
                        store.personas.add(persona.copy(nombre = remota.receptor,
                            dni = remota.dni_receptor, correo = remota.correo_receptor))
                    }
                    val entrega = Entrega(
                        id = remota.id,
                        objetoEncontradoId = reclamacion.objetoEncontradoId,
                        reclamacionId = reclamacion.id,
                        receptorId = reclamacion.personaAtendidaId,
                        administradorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
                        fechaConfirmacion = remota.entregado_en,
                        firmaCapturada = !remota.firma_url.isNullOrBlank(),
                        estado = EstadoEntrega.CONFIRMADA,
                        idOperacion = remota.codigo_entrega,
                        firmaReferencia = remota.firma_url,
                        receptorDni = remota.dni_receptor,
                        receptorCorreo = remota.correo_receptor
                    )
                    store.entregas.removeAll { it.id == entrega.id || it.reclamacionId == entrega.reclamacionId }
                    store.entregas.add(entrega)
                    store.operacionesEntregas.add(remota.codigo_entrega)
                }
                PersistenciaLocal.guardarReportes(store.reportesPerdida)
                PersistenciaLocal.guardarReclamaciones(store.reclamaciones)
                PersistenciaLocal.guardarPersonas(store.personas)
                PersistenciaLocal.guardarEntregas(store.entregas)
            }.onFailure { Log.e("UTPObjectsSync", "No se pudieron refrescar los casos", it) }
            Handler(Looper.getMainLooper()).post(alTerminar)
        }
    }

    /** Confirma remotamente antes de registrar la entrega en el repositorio local. */
    fun confirmarEntrega(
        reclamacion: Reclamacion,
        receptor: String,
        dni: String,
        correo: String,
        firma: Bitmap,
        codigoOperacion: String,
        alTerminar: (Result<String>) -> Unit
    ) {
        val bytes = ByteArrayOutputStream().use { salida ->
            check(firma.compress(Bitmap.CompressFormat.PNG, 100, salida)) { "No se pudo preparar la firma" }
            salida.toByteArray()
        }
        executor.execute {
            val resultado = runCatching {
                val usuario = requireNotNull(SesionRemota.usuarioId) { "Inicia sesión de nuevo" }
                val token = requireNotNull(SesionRemota.accessToken) { "Sesión caducada" }
                enviarReclamacion(reclamacion, usuario)
                val remota = SupabaseClient.casos.buscarReclamacion("eq.${reclamacion.id}").execute()
                check(remota.isSuccessful && remota.body().orEmpty().firstOrNull()?.decision == "APROBADA") {
                    "La reclamación todavía no está aprobada en Supabase"
                }
                val rutaFirma = "firmas/$codigoOperacion.png"
                val subida = Request.Builder()
                    .url("${SupabaseConfig.AUTH_URL.substringBefore("/auth/v1/")}/storage/v1/object/evidencias-utp-objects/$rutaFirma")
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .post(bytes.toRequestBody("image/png".toMediaType()))
                    .build()
                clienteArchivos.newCall(subida).execute().use { respuesta ->
                    check(respuesta.isSuccessful || respuesta.code == 409) {
                        "No se pudo guardar la firma: HTTP ${respuesta.code}"
                    }
                }
                val respuesta = SupabaseClient.casos.confirmarEntrega(
                    ConfirmarEntregaRemota(
                        p_codigo_reclamacion = reclamacion.id,
                        p_codigo_entrega = codigoOperacion,
                        p_receptor = receptor,
                        p_dni_receptor = dni,
                        p_correo_receptor = correo,
                        p_firma_url = rutaFirma
                    )
                ).execute()
                check(respuesta.isSuccessful) {
                    "No se confirmó la entrega: HTTP ${respuesta.code()} ${respuesta.errorBody()?.string()}"
                }
                requireNotNull(respuesta.body()) { "Supabase no devolvió el ID de entrega" }
            }
            Handler(Looper.getMainLooper()).post { alTerminar(resultado) }
        }
    }

    fun sincronizarPendientes() {
        val usuario = SesionRemota.usuarioId ?: return
        executor.execute {
            val store = AlmacenLocal.instancia
            store.reportesPerdida.toList()
                .filter { it.id.length == 36 && it.registradoPorRemoto == usuario }
                .forEach { reporte ->
                    runCatching { enviarReporte(reporte, usuario) }
                        .onFailure { Log.e("UTPObjectsSync", "Reporte ${reporte.id} pendiente de sincronización", it) }
                }
            store.reclamaciones.toList()
                .filter { it.id.startsWith("rec-") && it.reportePerdidaId != null }
                .forEach { reclamacion ->
                    runCatching { enviarReclamacion(reclamacion, usuario) }
                        .onFailure { Log.e("UTPObjectsSync", "Reclamación ${reclamacion.id} pendiente de sincronización", it) }
                }
        }
    }

    private fun enviarReporte(reporte: ReportePerdida, usuario: String): ReporteRemoto {
        require(reporte.registradoPorRemoto == usuario) { "El reporte debe sincronizarlo su creador" }
        val existente = SupabaseClient.casos.buscarReporte("eq.${reporte.id}")
            .execute().also { check(it.isSuccessful) { "Consulta de reporte: HTTP ${it.code()}" } }
            .body().orEmpty().firstOrNull()
        if (existente != null) return existente
        val persona = AlmacenLocal.instancia.personas.firstOrNull { it.id == reporte.personaAtendidaId }
        val respuesta = SupabaseClient.casos.crearReporte(
            ReporteRemoto(
                codigo_reporte = reporte.id,
                persona_atendida = persona?.nombre ?: "Solicitante atendido",
                contacto = reporte.contacto.orEmpty().ifBlank { "Contacto pendiente" },
                descripcion = reporte.descripcion,
                lugar_aproximado = reporte.lugarAproximado,
                estado = when (reporte.estado.name) {
                    "COINCIDENCIA_SUGERIDA" -> "COINCIDENCIA_SUGERIDA"
                    "RECUPERADO" -> "RECUPERADO"
                    else -> "BUSCANDO"
                },
                registrado_por = usuario
            )
        ).execute()
        check(respuesta.isSuccessful) { "Creación de reporte: HTTP ${respuesta.code()} ${respuesta.errorBody()?.string()}" }
        return requireNotNull(respuesta.body()?.firstOrNull()) { "Supabase no devolvió el reporte" }
    }

    private fun enviarReclamacion(reclamacion: Reclamacion, usuario: String) {
        val reporteLocal = AlmacenLocal.instancia.reportesPerdida
            .firstOrNull { it.id == reclamacion.reportePerdidaId } ?: return
        if (reporteLocal.id.length != 36) return // Los casos semilla no se publican como casos reales.
        if (reporteLocal.registradoPorRemoto == null) return
        val objetoLocal = AlmacenLocal.instancia.objetos
            .firstOrNull { it.id == reclamacion.objetoEncontradoId } ?: return
        val codigo = objetoLocal.codigoCustodia.takeIf { it.isNotBlank() } ?: return
        val reporteRemoto = if (reporteLocal.registradoPorRemoto == usuario) enviarReporte(reporteLocal, usuario)
        else SupabaseClient.casos.buscarReporte("eq.${reporteLocal.id}").execute()
            .body().orEmpty().firstOrNull() ?: return
        val objetoRespuesta = SupabaseClient.api.buscarObjetoPorCodigo("eq.$codigo").execute()
        check(objetoRespuesta.isSuccessful) { "Consulta de objeto: HTTP ${objetoRespuesta.code()}" }
        val objetoRemoto = objetoRespuesta.body()?.firstOrNull() ?: return
        val reporteId = reporteRemoto.id ?: return
        val objetoId = objetoRemoto.id ?: return
        val existenteRespuesta = SupabaseClient.casos.buscarReclamacion("eq.${reclamacion.id}").execute()
        check(existenteRespuesta.isSuccessful) { "Consulta de reclamación: HTTP ${existenteRespuesta.code()}" }
        val existente = existenteRespuesta.body()?.firstOrNull()
        if (existente == null) {
            val persona = AlmacenLocal.instancia.personas.firstOrNull { it.id == reclamacion.personaAtendidaId }
            val respuesta = SupabaseClient.casos.crearReclamacion(
                ReclamacionRemota(
                    codigo_reclamacion = reclamacion.id,
                    reporte_perdida_id = reporteId,
                    objeto_encontrado_id = objetoId,
                    solicitante = persona?.nombre ?: "Solicitante atendido",
                    evidencia_aportada = reclamacion.caracteristicasDeclaradas,
                    decision = "PENDIENTE_VERIFICACION"
                )
            ).execute()
            check(respuesta.isSuccessful) { "Creación de reclamación: HTTP ${respuesta.code()} ${respuesta.errorBody()?.string()}" }
        }
        if (SesionRemota.rol == "ADMINISTRADOR_SAE" &&
            reclamacion.estado != EstadoReclamacion.ATENDIDA &&
            (reclamacion.motivoDecision != null || reclamacion.preguntas.isNotEmpty())) {
            val id = existente?.id ?: SupabaseClient.casos.buscarReclamacion("eq.${reclamacion.id}")
                .execute().body()?.firstOrNull()?.id ?: return
            val cambios = mapOf(
                "decision" to reclamacion.estado.name,
                "motivo_decision" to reclamacion.motivoDecision,
                "preguntas_verificacion" to guardarPreguntas(reclamacion.preguntas),
                "revisada_por" to usuario
            )
            val respuesta = SupabaseClient.casos.actualizarReclamacion("eq.$id", cambios).execute()
            check(respuesta.isSuccessful) { "Decisión de reclamación: HTTP ${respuesta.code()} ${respuesta.errorBody()?.string()}" }
        }
    }

    private fun guardarPreguntas(preguntas: List<PreguntaVerificacion>): String = JSONArray().apply {
        preguntas.forEach { item ->
            put(JSONObject().put("id", item.id).put("pregunta", item.pregunta)
                .put("respuesta", item.respuesta ?: JSONObject.NULL))
        }
    }.toString()

    private fun leerPreguntas(valor: String?, locales: List<PreguntaVerificacion>): List<PreguntaVerificacion> {
        if (valor.isNullOrBlank()) return locales
        if (!valor.trimStart().startsWith("[")) {
            return locales.ifEmpty {
                valor.lines().filter { it.isNotBlank() }.mapIndexed { indice, pregunta ->
                    PreguntaVerificacion("pregunta-$indice", pregunta)
                }
            }
        }
        return runCatching {
            val arreglo = JSONArray(valor)
            (0 until arreglo.length()).mapNotNull { indice ->
                val item = arreglo.optJSONObject(indice) ?: return@mapNotNull null
                val pregunta = item.optString("pregunta").trim()
                if (pregunta.isBlank()) null else PreguntaVerificacion(
                    id = item.optString("id", "pregunta-$indice"),
                    pregunta = pregunta,
                    respuesta = item.optString("respuesta").takeIf { it.isNotBlank() && it != "null" }
                )
            }
        }.getOrDefault(locales)
    }
}
