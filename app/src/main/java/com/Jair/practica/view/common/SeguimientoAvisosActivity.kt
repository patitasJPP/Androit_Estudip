package com.Jair.practica.view.common

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.model.remote.SesionRemota
import com.Jair.practica.model.remote.SincronizacionCasos
import com.Jair.practica.model.remote.SincronizacionRemota

/** Seguimiento de registros reales; cada registrador ve únicamente los casos que creó. */
class SeguimientoAvisosActivity : AppCompatActivity() {
    private enum class Filtro { TODOS, PENDIENTES, COMPLETADOS }
    private data class Aviso(val titulo: String, val detalle: String, val completado: Boolean)

    private val rol by lazy { LoginRole.fromCode(intent.getStringExtra("rol")) }
    private var filtro = Filtro.TODOS
    private var actualizando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mostrar()
        actualizar()
    }

    override fun onResume() {
        super.onResume()
        if (!actualizando) actualizar()
    }

    private fun actualizar() {
        if (actualizando) return
        actualizando = true
        mostrar()
        SincronizacionRemota.refrescarObjetos {
            SincronizacionCasos.refrescarCasos {
                actualizando = false
                if (!isFinishing && !isDestroyed) mostrar()
            }
        }
    }

    private fun avisos(): List<Aviso> {
        val store = AlmacenLocal.instancia
        val usuario = SesionRemota.usuarioId ?: return emptyList()
        val admin = rol == LoginRole.ADMINISTRADOR_SAE
        val objetos = store.objetos.filter {
            it.registradoPor.length == 36 && (admin || it.registradoPor == usuario)
        }
        val reportes = store.reportesPerdida.filter {
            it.registradoPorRemoto != null && (admin || it.registradoPorRemoto == usuario)
        }
        val idsReportes = reportes.mapTo(mutableSetOf()) { it.id }
        val reclamaciones = store.reclamaciones.filter { reclamacion ->
            reclamacion.reportePerdidaId?.let(idsReportes::contains) == true
        }
        val idsReclamaciones = reclamaciones.mapTo(mutableSetOf()) { it.id }
        val entregas = store.entregas.filter { it.reclamacionId in idsReclamaciones }

        return buildList {
            objetos.forEach { objeto ->
                val estado = when (objeto.estado) {
                    EstadoObjeto.HALLAZGO_REPORTADO, EstadoObjeto.PENDIENTE_RECEPCION -> "Pendiente de recepción"
                    EstadoObjeto.BAJO_CUSTODIA -> "Bajo custodia"
                    EstadoObjeto.ENTREGADO -> "Entregado"
                    else -> objeto.estado.name.replace('_', ' ')
                }
                add(Aviso("Objeto encontrado · $estado",
                    "${objeto.descripcion} · ${objeto.codigoCustodia.ifBlank { "Sin código de custodia" }}",
                    objeto.estado == EstadoObjeto.ENTREGADO))
            }
            reportes.forEach { reporte ->
                val estado = when (reporte.estado) {
                    EstadoReportePerdida.RECUPERADO -> "Recuperado"
                    EstadoReportePerdida.COINCIDENCIA_SUGERIDA -> "Coincidencia sugerida"
                    EstadoReportePerdida.BUSCANDO -> "En búsqueda"
                    else -> reporte.estado.name.replace('_', ' ')
                }
                add(Aviso("Reporte de pérdida · $estado", reporte.descripcion,
                    reporte.estado == EstadoReportePerdida.RECUPERADO || reporte.estado == EstadoReportePerdida.CERRADO))
            }
            reclamaciones.forEach { reclamacion ->
                val objeto = store.objetos.firstOrNull { it.id == reclamacion.objetoEncontradoId }
                val estado = when (reclamacion.estado) {
                    EstadoReclamacion.PENDIENTE_VERIFICACION -> "Pendiente de verificación"
                    EstadoReclamacion.APROBADA -> "Aprobada; pendiente de entrega"
                    EstadoReclamacion.ATENDIDA -> "Atendida"
                    EstadoReclamacion.RECHAZADA -> "Rechazada"
                    else -> reclamacion.estado.name.replace('_', ' ')
                }
                add(Aviso("Reclamación · $estado",
                    "${objeto?.descripcion ?: reclamacion.id} · ${reclamacion.id}",
                    reclamacion.estado == EstadoReclamacion.ATENDIDA || reclamacion.estado == EstadoReclamacion.RECHAZADA))
            }
            entregas.forEach { entrega ->
                val objeto = store.objetos.firstOrNull { it.id == entrega.objetoEncontradoId }
                add(Aviso("Entrega confirmada",
                    "${objeto?.descripcion ?: entrega.id} · ${entrega.idOperacion ?: entrega.id}", true))
            }
        }.asReversed()
    }

    private fun mostrar() {
        val (scroll, root) = AppUi.screen(this)
        root.addView(AppUi.header(this, "Seguimiento y avisos", "UTP · Campus Ate") { finish() })
        root.addView(TextView(this).apply {
            text = if (actualizando) "Actualizando casos desde Supabase…" else "Estados de los casos registrados"
            textSize = 12f
            setTextColor(AppUi.MUTED)
        })

        val filtros = AppUi.card(this, 14)
        filtros.addView(TextView(this).apply {
            text = "Filtrar actividad"; textSize = 14f; typeface = Typeface.DEFAULT_BOLD; setTextColor(AppUi.INK)
        })
        val fila = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(Filtro.TODOS to "Todos", Filtro.PENDIENTES to "Pendientes", Filtro.COMPLETADOS to "Completados").forEach { (tipo, nombre) ->
            fila.addView(TextView(this).apply {
                text = nombre; textSize = 11f; gravity = Gravity.CENTER
                setTextColor(if (filtro == tipo) android.graphics.Color.WHITE else AppUi.INK)
                background = AppUi.rounded(if (filtro == tipo) AppUi.RED else 0xFFE8ECF3.toInt(), 24)
                setPadding(AppUi.dp(this@SeguimientoAvisosActivity, 7), AppUi.dp(this@SeguimientoAvisosActivity, 10),
                    AppUi.dp(this@SeguimientoAvisosActivity, 7), AppUi.dp(this@SeguimientoAvisosActivity, 10))
                setOnClickListener { filtro = tipo; mostrar() }
            }, LinearLayout.LayoutParams(0, -2, 1f).apply {
                marginEnd = AppUi.dp(this@SeguimientoAvisosActivity, 5)
            })
        }
        filtros.addView(fila, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = AppUi.dp(this@SeguimientoAvisosActivity, 10)
        })
        root.addView(filtros)

        val visibles = avisos().filter {
            when (filtro) {
                Filtro.TODOS -> true
                Filtro.PENDIENTES -> !it.completado
                Filtro.COMPLETADOS -> it.completado
            }
        }
        if (visibles.isEmpty()) {
            val vacio = AppUi.card(this)
            vacio.addView(TextView(this).apply {
                text = if (actualizando) "Cargando casos…" else "No hay casos en este filtro."
                textSize = 14f; setTextColor(AppUi.MUTED)
            })
            root.addView(vacio)
        }
        visibles.forEach { aviso ->
            val tarjeta = AppUi.card(this, 10)
            val cabecera = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            }
            cabecera.addView(TextView(this).apply {
                text = aviso.titulo; textSize = 14f; typeface = Typeface.DEFAULT_BOLD; setTextColor(AppUi.INK)
            }, LinearLayout.LayoutParams(0, -2, 1f))
            cabecera.addView(AppUi.status(this, if (aviso.completado) "COMPLETADO" else "EN CURSO",
                if (aviso.completado) AppUi.GREEN else AppUi.AMBER))
            tarjeta.addView(cabecera)
            tarjeta.addView(TextView(this).apply {
                text = aviso.detalle; textSize = 12f; setTextColor(AppUi.MUTED)
                setPadding(0, AppUi.dp(this@SeguimientoAvisosActivity, 8), 0, 0)
            })
            root.addView(tarjeta)
        }
        root.addView(AppUi.primary(this, if (actualizando) "Actualizando…" else "Actualizar estados").apply {
            isEnabled = !actualizando
            setOnClickListener { actualizar() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@SeguimientoAvisosActivity, 16) })

        root.addView(AppUi.bottomNav(this,
            { finish() },
            {
                if (rol == LoginRole.ADMINISTRADOR_SAE) {
                    abrir(com.Jair.practica.view.devolucion.HistorialEntregasActivity::class.java)
                } else {
                    filtro = Filtro.COMPLETADOS
                    mostrar()
                }
            },
            { abrir(ReportesActivity::class.java) },
            { abrir(PerfilActivity::class.java) }
        ), LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = AppUi.dp(this@SeguimientoAvisosActivity, 18)
        })
        setContentView(scroll)
    }

    private fun abrir(destino: Class<out AppCompatActivity>) {
        startActivity(Intent(this, destino).apply {
            putExtra("rol", rol.code)
            putExtra("usuarioId", intent.getStringExtra("usuarioId"))
        })
    }
}
