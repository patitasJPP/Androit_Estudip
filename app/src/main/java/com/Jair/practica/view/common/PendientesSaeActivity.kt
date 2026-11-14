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
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.model.remote.SesionRemota
import com.Jair.practica.model.remote.SincronizacionCasos
import com.Jair.practica.model.remote.SincronizacionRemota
import com.Jair.practica.view.devolucion.ConfirmarDevolucionActivity
import com.Jair.practica.view.devolucion.HistorialEntregasActivity
import com.Jair.practica.view.devolucion.VerificarReclamacionActivity
import com.Jair.practica.view.hallazgo.ConfirmarRecepcionActivity

/** Bandeja SAE: el administrador elige el caso antes de confirmar o verificar. */
class PendientesSaeActivity : AppCompatActivity() {
    private var cargando = false
    private val recepciones get() = intent.getStringExtra(EXTRA_TIPO) == TIPO_RECEPCIONES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LoginRole.fromCode(intent.getStringExtra("rol")) != LoginRole.ADMINISTRADOR_SAE ||
            SesionRemota.usuarioId == null) {
            finish()
            return
        }
        mostrar()
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing) actualizar()
    }

    private fun actualizar() {
        if (cargando) return
        cargando = true
        mostrar()
        SincronizacionRemota.refrescarObjetos {
            SincronizacionCasos.refrescarCasos {
                cargando = false
                if (!isFinishing && !isDestroyed) mostrar()
            }
        }
    }

    private fun mostrar() {
        val (scroll, root) = AppUi.screen(this)
        val titulo = if (recepciones) "Recepciones pendientes" else "Reclamaciones por atender"
        root.addView(AppUi.header(this, titulo, "Administrador SAE · Campus Ate") { finish() })
        root.addView(TextView(this).apply {
            text = if (cargando) "Actualizando casos desde Supabase…" else
                if (recepciones) "Selecciona el hallazgo que recibiste físicamente."
                else "Selecciona una reclamación para verificarla o continuar la entrega."
            textSize = 13f
            setTextColor(AppUi.MUTED)
        })

        val store = AlmacenLocal.instancia
        if (recepciones) {
            val pendientes = store.objetos.filter {
                (it.estado == EstadoObjeto.HALLAZGO_REPORTADO || it.estado == EstadoObjeto.PENDIENTE_RECEPCION) &&
                    (it.id.startsWith("hallazgo-") || it.registradoPor.length == 36)
            }.distinctBy { it.id }
            if (pendientes.isEmpty()) vacio(root, "No hay hallazgos pendientes de recepción.")
            pendientes.forEach { objeto ->
                tarjeta(root, objeto.descripcion,
                    "Lugar: ${objeto.lugarHallazgo ?: "No indicado"}\nCódigo de custodia: pendiente",
                    "Confirmar recepción") {
                    startActivity(Intent(this, ConfirmarRecepcionActivity::class.java).apply {
                        putExtra("ID_OBJETO_GUARDADO", objeto.id)
                        putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
                    })
                }
            }
        } else {
            val reclamaciones = store.reclamaciones.filter { reclamacion ->
                (reclamacion.estado == EstadoReclamacion.PENDIENTE_VERIFICACION ||
                    reclamacion.estado == EstadoReclamacion.APROBADA) &&
                    store.reportesPerdida.any { reporte ->
                        reporte.id == reclamacion.reportePerdidaId && reporte.id.length == 36
                    } &&
                    store.objetos.any { objeto ->
                        objeto.id == reclamacion.objetoEncontradoId && objeto.estado == EstadoObjeto.BAJO_CUSTODIA
                    }
            }
            if (reclamaciones.isEmpty()) vacio(root, "No hay reclamaciones pendientes de atención.")
            reclamaciones.forEach { reclamacion ->
                val objeto = store.objetos.firstOrNull { it.id == reclamacion.objetoEncontradoId }
                val reporte = store.reportesPerdida.firstOrNull { it.id == reclamacion.reportePerdidaId }
                val aprobada = reclamacion.estado == EstadoReclamacion.APROBADA
                val descripcion = objeto?.descripcion ?: "Objeto no disponible"
                val detalle = "Objeto: $descripcion\nCódigo: ${objeto?.codigoCustodia ?: "Sin código"}\nPérdida reportada: ${reporte?.descripcion ?: "Sin descripción"}"
                tarjeta(root, if (aprobada) "Aprobada · pendiente de entrega" else "Pendiente de verificación",
                    detalle, if (aprobada) "Continuar devolución" else "Verificar reclamación") {
                    if (aprobada) {
                        startActivity(Intent(this, ConfirmarDevolucionActivity::class.java).apply {
                            putExtra(ConfirmarDevolucionActivity.EXTRA_RECLAMACION, reclamacion.id)
                            putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
                        })
                    } else {
                        startActivity(Intent(this, VerificarReclamacionActivity::class.java).apply {
                            putExtra(VerificarReclamacionActivity.EXTRA_RECLAMACION, reclamacion.id)
                            putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
                        })
                    }
                }
            }
        }
        root.addView(AppUi.secondary(this, if (cargando) "Actualizando…" else "Actualizar lista").apply {
            isEnabled = !cargando
            setOnClickListener { actualizar() }
        }, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = AppUi.dp(this@PendientesSaeActivity, 16)
        })
        root.addView(AppUi.bottomNav(this,
            { finish() },
            { abrir(HistorialEntregasActivity::class.java) },
            { abrir(ReportesActivity::class.java) },
            { abrir(PerfilActivity::class.java) }
        ), LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = AppUi.dp(this@PendientesSaeActivity, 18)
        })
        setContentView(scroll)
    }

    private fun vacio(root: LinearLayout, texto: String) {
        val card = AppUi.card(this)
        card.addView(TextView(this).apply {
            text = if (cargando) "Cargando casos…" else texto
            textSize = 14f
            setTextColor(AppUi.MUTED)
        })
        root.addView(card)
    }

    private fun tarjeta(root: LinearLayout, titulo: String, detalle: String, accion: String, alPulsar: () -> Unit) {
        val card = AppUi.card(this, 12)
        val fila = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        fila.addView(TextView(this).apply {
            text = titulo; textSize = 15f; typeface = Typeface.DEFAULT_BOLD; setTextColor(AppUi.INK)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        fila.addView(AppUi.status(this, "PENDIENTE", AppUi.AMBER))
        card.addView(fila)
        card.addView(TextView(this).apply {
            text = detalle; textSize = 12f; setTextColor(AppUi.MUTED)
            setPadding(0, AppUi.dp(this@PendientesSaeActivity, 9), 0, 0)
        })
        card.addView(AppUi.primary(this, accion).apply { setOnClickListener { alPulsar() } },
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@PendientesSaeActivity, 12) })
        root.addView(card)
    }

    private fun abrir(destino: Class<out AppCompatActivity>) {
        startActivity(Intent(this, destino).apply {
            putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
            putExtra("usuarioId", intent.getStringExtra("usuarioId"))
        })
    }

    companion object {
        const val EXTRA_TIPO = "tipoPendientes"
        const val TIPO_RECEPCIONES = "recepciones"
        const val TIPO_RECLAMACIONES = "reclamaciones"
    }
}
