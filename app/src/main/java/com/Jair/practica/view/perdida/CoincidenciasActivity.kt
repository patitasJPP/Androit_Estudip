package com.Jair.practica.view.perdida

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.model.local.LocalReportePerdidaRepository
import com.Jair.practica.model.remote.SincronizacionRemota
import com.Jair.practica.viewmodel.perdida.ReportePerdidaViewModel
import com.Jair.practica.view.common.AppUi

class CoincidenciasActivity : AppCompatActivity() {
    private val viewModel: ReportePerdidaViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val (scroll, root) = AppUi.screen(this)
        root.addView(AppUi.header(this, "Posibles coincidencias", "Módulo de pérdidas") { finish() })
        root.addView(TextView(this).apply { text = "02  REVISAR COINCIDENCIAS"; textSize = 10f; setTextColor(AppUi.RED) })
        val resumen = AppUi.card(this, 14)
        resumen.addView(TextView(this).apply { text = "Resultado de búsqueda"; textSize = 16f; setTextColor(AppUi.INK); typeface = android.graphics.Typeface.DEFAULT_BOLD })
        resumen.addView(TextView(this).apply { text = "Las sugerencias se basan en descripción y lugar. El SAE debe verificar antes de autorizar cualquier devolución."; textSize = 12f; setTextColor(AppUi.MUTED); setPadding(0, AppUi.dp(this@CoincidenciasActivity, 5), 0, 0) })
        root.addView(resumen)
        val contenido = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(contenido)
        setContentView(scroll)
        val reporte = LocalReportePerdidaRepository().findById(intent.getStringExtra("reporteId").orEmpty())
        if (reporte == null) { contenido.addView(emptyState("No se encontró el reporte.")); return }
        val actualizar = AppUi.secondary(this, "Actualizar búsqueda")
        root.addView(actualizar, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = AppUi.dp(this@CoincidenciasActivity, 14)
        })
        root.addView(AppUi.bottomNav(this, { finish() }, { android.widget.Toast.makeText(this, "La devolución requiere verificación del SAE.", android.widget.Toast.LENGTH_SHORT).show() }, { android.widget.Toast.makeText(this, "Ya estás revisando coincidencias.", android.widget.Toast.LENGTH_SHORT).show() }, { android.widget.Toast.makeText(this, "Perfil del usuario autenticado.", android.widget.Toast.LENGTH_SHORT).show() }), LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@CoincidenciasActivity, 22) })
        fun buscarActualizado() {
            actualizar.isEnabled = false
            contenido.removeAllViews()
            contenido.addView(AppUi.card(this, 12).apply {
                addView(TextView(this@CoincidenciasActivity).apply {
                    text = "Actualizando objetos bajo custodia…"
                    textSize = 13f
                    setTextColor(AppUi.MUTED)
                })
            })
            SincronizacionRemota.refrescarObjetos {
                if (!isFinishing && !isDestroyed) {
                    viewModel.buscar(reporte)
                    actualizar.isEnabled = true
                }
            }
        }
        actualizar.setOnClickListener { buscarActualizado() }
        buscarActualizado()
        viewModel.coincidencias.observe(this) { lista ->
            contenido.removeAllViews()
            if (lista.isEmpty()) {
                contenido.addView(emptyState("No hay coincidencias por ahora. Comprueba que el SAE haya confirmado la recepción y que la descripción y el lugar coincidan. El reporte queda en seguimiento."))
            } else lista.forEach { objeto ->
                val card = AppUi.card(this, 12)
                val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL }
                head.addView(TextView(this).apply { text = objeto.categoria; textSize = 15f; setTextColor(AppUi.INK); typeface = android.graphics.Typeface.DEFAULT_BOLD }, LinearLayout.LayoutParams(0, -2, 1f))
                head.addView(AppUi.status(this, "SUGERIDA", AppUi.AMBER))
                card.addView(head)
                card.addView(TextView(this).apply { text = objeto.descripcion; textSize = 13f; setTextColor(AppUi.INK); setPadding(0, AppUi.dp(this@CoincidenciasActivity, 8), 0, 0) })
                card.addView(TextView(this).apply { text = "Código ${objeto.codigoCustodia}\nLugar: ${objeto.lugarHallazgo}\nEstado: ${objeto.estado}"; textSize = 12f; setTextColor(AppUi.MUTED); setPadding(0, AppUi.dp(this@CoincidenciasActivity, 5), 0, 0) })
                card.addView(AppUi.secondary(this, "Solicitar verificación al SAE").apply {
                    if (viewModel.tieneSolicitud(reporte.id, objeto.id)) {
                        text = "Verificación solicitada"
                        isEnabled = false
                    }
                    setOnClickListener {
                        isEnabled = false
                        val mensaje = viewModel.solicitarVerificacion(reporte, objeto.id)
                        android.widget.Toast.makeText(this@CoincidenciasActivity, mensaje, android.widget.Toast.LENGTH_LONG).show()
                        if (viewModel.tieneSolicitud(reporte.id, objeto.id)) {
                            text = "Verificación solicitada"
                        } else {
                            isEnabled = true
                        }
                    }
                }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@CoincidenciasActivity, 12) })
                contenido.addView(card)
            }
        }
    }

    private fun emptyState(message: String): View = AppUi.card(this, 16).apply {
        addView(TextView(this@CoincidenciasActivity).apply { text = "Sin coincidencias confirmadas"; textSize = 16f; setTextColor(AppUi.INK); typeface = android.graphics.Typeface.DEFAULT_BOLD })
        addView(TextView(this@CoincidenciasActivity).apply { text = message; textSize = 12f; setTextColor(AppUi.MUTED); setPadding(0, AppUi.dp(this@CoincidenciasActivity, 7), 0, 0) })
        addView(AppUi.primary(this@CoincidenciasActivity, "Volver al reporte").apply { setOnClickListener { finish() } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@CoincidenciasActivity, 14) })
    }
}
