package com.Jair.practica.model.remote

import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.PersistenciaLocal
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.Executors
import java.util.UUID
import java.nio.charset.StandardCharsets

/** Sincroniza hallazgos sin bloquear las pantallas. */
object SincronizacionRemota {
    private val executor = Executors.newSingleThreadExecutor()

    fun enviarHallazgo(objeto: ObjetoEncontrado) {
        val usuario = SesionRemota.usuarioId ?: return
        executor.execute {
            runCatching {
                // Los objetos descargados de otro dispositivo ya tienen el UUID de Supabase.
                val idRemoto = runCatching { UUID.fromString(objeto.id).toString() }.getOrElse {
                    UUID.nameUUIDFromBytes(
                        "utp-objects:${objeto.id}".toByteArray(StandardCharsets.UTF_8)
                    ).toString()
                }
                val existente = objeto.codigoCustodia.takeIf { it.isNotBlank() }
                    ?.let { codigo -> SupabaseClient.api.buscarObjetoPorCodigo("eq.$codigo").execute().body()?.firstOrNull() }
                    ?: SupabaseClient.api.buscarObjetoPorId("eq.$idRemoto").execute().body()?.firstOrNull()
                val datos = ObjetoEncontradoRemoto(
                        id = idRemoto,
                        codigo_custodia = objeto.codigoCustodia.takeIf { it.isNotBlank() },
                        categoria = objeto.categoria,
                        descripcion = objeto.descripcion,
                        lugar_hallazgo = objeto.lugarHallazgo,
                        estado = when (objeto.estado.name) {
                            "BAJO_CUSTODIA" -> "BAJO_CUSTODIA"
                            "ENTREGADO" -> "ENTREGADO"
                            else -> "HALLAZGO_REPORTADO"
                        },
                        registrado_por = existente?.registrado_por ?: usuario
                    )
                val respuesta = if (existente?.id != null) {
                    SupabaseClient.api.actualizarObjeto("eq.${existente.id}", datos.copy(id = null)).execute()
                } else {
                    SupabaseClient.api.registrarObjeto(datos).execute()
                }
                check(respuesta.isSuccessful) { "No se pudo sincronizar el hallazgo: HTTP ${respuesta.code()}" }
                SincronizacionCasos.sincronizarPendientes()
            }.onFailure { Log.e("UTPObjectsSync", "Error al sincronizar hallazgo ${objeto.id}", it) }
        }
    }

    fun refrescarObjetos(onComplete: () -> Unit = {}) {
        if (SesionRemota.accessToken == null) return
        executor.execute {
            runCatching {
                val respuesta = SupabaseClient.api.consultarObjetos().execute()
                if (respuesta.isSuccessful) {
                    respuesta.body().orEmpty().forEach { remoto ->
                        val id = remoto.id ?: return@forEach
                        val codigo = remoto.codigo_custodia.orEmpty()
                        val actual = AlmacenLocal.instancia.objetos.firstOrNull { local ->
                            local.id == id ||
                                (codigo.isNotBlank() && local.codigoCustodia == codigo) ||
                                runCatching {
                                    UUID.nameUUIDFromBytes(
                                        "utp-objects:${local.id}".toByteArray(StandardCharsets.UTF_8)
                                    ).toString() == id
                                }.getOrDefault(false)
                        }
                        val estado = runCatching { EstadoObjeto.valueOf(remoto.estado) }.getOrDefault(EstadoObjeto.HALLAZGO_REPORTADO)
                        val objeto = (actual ?: ObjetoEncontrado(
                            id = id,
                            codigoCustodia = codigo,
                            categoria = remoto.categoria,
                            descripcion = remoto.descripcion,
                            registradoPor = remoto.registrado_por
                        )).copy(
                            codigoCustodia = if (codigo.isNotBlank()) codigo else actual?.codigoCustodia.orEmpty(),
                            categoria = remoto.categoria,
                            descripcion = remoto.descripcion,
                            registradoPor = remoto.registrado_por,
                            lugarHallazgo = remoto.lugar_hallazgo,
                            fechaHallazgo = remoto.fecha_hallazgo,
                            estado = if (actual?.estado == EstadoObjeto.ENTREGADO ||
                                (actual?.estado == EstadoObjeto.BAJO_CUSTODIA && estado == EstadoObjeto.HALLAZGO_REPORTADO))
                                actual.estado else estado
                        )
                        val idx = AlmacenLocal.instancia.objetos.indexOfFirst { it.id == objeto.id }
                        if (idx >= 0) AlmacenLocal.instancia.objetos[idx] = objeto else AlmacenLocal.instancia.objetos.add(objeto)
                    }
                    val store = AlmacenLocal.instancia
                    val pendienteActual = store.ultimoObjetoPendienteId
                    if (pendienteActual == null || store.objetos.none { it.id == pendienteActual &&
                            (it.estado == EstadoObjeto.HALLAZGO_REPORTADO || it.estado == EstadoObjeto.PENDIENTE_RECEPCION) }) {
                        store.ultimoObjetoPendienteId = store.objetos.lastOrNull {
                            it.estado == EstadoObjeto.HALLAZGO_REPORTADO || it.estado == EstadoObjeto.PENDIENTE_RECEPCION
                        }?.id
                    }
                    PersistenciaLocal.guardarObjetos(AlmacenLocal.instancia.objetos)
                }
            }
            Handler(Looper.getMainLooper()).post(onComplete)
        }
    }
}
