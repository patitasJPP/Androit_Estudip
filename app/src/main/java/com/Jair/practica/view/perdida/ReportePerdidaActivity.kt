package com.Jair.practica.view.perdida

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.model.remote.SesionRemota
import com.Jair.practica.viewmodel.perdida.ReportePerdidaViewModel
import com.Jair.practica.view.common.AppUi

/** RF-05/RF-06: documentar una pérdida y consultar sugerencias de coincidencia. */
class ReportePerdidaActivity : AppCompatActivity() {
    private val viewModel: ReportePerdidaViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rol = LoginRole.fromCode(intent.getStringExtra("rol") ?: LoginRole.PERSONAL_REGISTRADOR.code)
        val usuario = intent.getStringExtra("usuarioId") ?: "usr-registrador-01"
        val (scroll, root) = AppUi.screen(this)
        root.addView(AppUi.header(this, "Reportar pérdida", "Módulo de pérdidas") { finish() })
        val progress = TextView(this).apply { text = "01  REPORTAR   ─────   02  REVISAR COINCIDENCIAS"; textSize = 10f; setTextColor(AppUi.RED) }
        root.addView(progress)

        val intro = AppUi.card(this, 14)
        intro.addView(TextView(this).apply { text = "Describe lo que perdiste"; textSize = 16f; setTextColor(AppUi.INK); typeface = android.graphics.Typeface.DEFAULT_BOLD })
        intro.addView(TextView(this).apply { text = "La información se conservará para buscar coincidencias y contactar al solicitante."; textSize = 12f; setTextColor(AppUi.MUTED); setPadding(0, AppUi.dp(this@ReportePerdidaActivity, 5), 0, 0) })
        root.addView(intro)

        val reportesPrevios = AlmacenLocal.instancia.reportesPerdida
            .filter { it.registradoPorRemoto == SesionRemota.usuarioId }
            .asReversed()
        if (reportesPrevios.isNotEmpty()) {
            val preferencias = getSharedPreferences("reportes_ocultos", MODE_PRIVATE)
            val clave = "ids_${SesionRemota.usuarioId}"
            val ocultos = preferencias.getStringSet(clave, emptySet())!!.toMutableSet()
            val anteriores = AppUi.card(this)
            var mostrarOcultos = false
            fun guardarOcultos() {
                preferencias.edit().putStringSet(clave, ocultos.toSet()).apply()
            }
            fun mostrarReportes() {
                anteriores.removeAllViews()
                anteriores.addView(TextView(this).apply {
                    text = "Reanudar un reporte"
                    textSize = 16f
                    setTextColor(AppUi.INK)
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                })
                val visibles = reportesPrevios.filterNot { it.id in ocultos }.take(5)
                if (visibles.isEmpty()) {
                    anteriores.addView(TextView(this).apply {
                        text = "No tienes reportes visibles. Puedes restaurar los ocultos."
                        setTextColor(AppUi.MUTED)
                    })
                }
                visibles.forEach { reporte ->
                    anteriores.addView(AppUi.secondary(this, "Buscar de nuevo: ${reporte.descripcion}").apply {
                        setOnClickListener {
                            startActivity(Intent(this@ReportePerdidaActivity, CoincidenciasActivity::class.java).apply {
                                putExtra("reporteId", reporte.id)
                            })
                        }
                    }, LinearLayout.LayoutParams(-1, -2).apply {
                        topMargin = AppUi.dp(this@ReportePerdidaActivity, 8)
                    })
                    anteriores.addView(TextView(this).apply {
                        text = "Ocultar este reporte"
                        textSize = 12f
                        setTextColor(AppUi.RED)
                        gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        minHeight = AppUi.dp(this@ReportePerdidaActivity, 40)
                        setOnClickListener { ocultos.add(reporte.id); guardarOcultos(); mostrarReportes() }
                    })
                }
                val reportesOcultos = reportesPrevios.filter { it.id in ocultos }
                if (reportesOcultos.isNotEmpty()) {
                    anteriores.addView(AppUi.secondary(this, if (mostrarOcultos) "Ocultar lista de reportes ocultos" else "Mostrar reportes ocultos (${reportesOcultos.size})").apply {
                        setOnClickListener { mostrarOcultos = !mostrarOcultos; mostrarReportes() }
                    })
                    if (mostrarOcultos) reportesOcultos.forEach { reporte ->
                        anteriores.addView(AppUi.secondary(this, "Restaurar: ${reporte.descripcion}").apply {
                            setOnClickListener { ocultos.remove(reporte.id); guardarOcultos(); mostrarReportes() }
                        }, LinearLayout.LayoutParams(-1, -2).apply {
                            topMargin = AppUi.dp(this@ReportePerdidaActivity, 8)
                        })
                    }
                }
            }
            mostrarReportes()
            root.addView(anteriores)
        }

        val datos = AppUi.card(this)
        datos.addView(AppUi.sectionTitle(this, "Descripción del objeto", true))
        val descripcion = AppUi.field(this, "Ej. Laptop negra Lenovo con stickers")
        descripcion.id = R.id.etPerdidaDescripcion
        datos.addView(descripcion)
        datos.addView(AppUi.sectionTitle(this, "Lugar aproximado", true).apply { setPadding(0, AppUi.dp(this@ReportePerdidaActivity, 15), 0, AppUi.dp(this@ReportePerdidaActivity, 7)) })
        val lugar = AppUi.field(this, "Pabellón, piso o aula")
        lugar.id = R.id.etPerdidaLugar
        datos.addView(lugar)
        datos.addView(AppUi.sectionTitle(this, "Medio de contacto", true).apply { setPadding(0, AppUi.dp(this@ReportePerdidaActivity, 15), 0, AppUi.dp(this@ReportePerdidaActivity, 7)) })
        val contacto = AppUi.field(this, "Correo o teléfono")
        contacto.id = R.id.etPerdidaContacto
        datos.addView(contacto)
        root.addView(datos)

        val nota = AppUi.card(this)
        nota.addView(TextView(this).apply { text = "Consejo para una mejor búsqueda"; textSize = 13f; typeface = android.graphics.Typeface.DEFAULT_BOLD; setTextColor(AppUi.INK) })
        nota.addView(TextView(this).apply { text = "Incluye color, marca, modelo, accesorios o una característica que no sea visible para todos."; textSize = 12f; setTextColor(AppUi.MUTED); setPadding(0, AppUi.dp(this@ReportePerdidaActivity, 6), 0, 0) })
        root.addView(nota)

        val guardar = AppUi.primary(this, "Guardar reporte y buscar coincidencias")
        guardar.id = R.id.btnGuardarPerdida
        guardar.setOnClickListener { guardar.isEnabled = false; viewModel.registrar(rol, usuario, descripcion.text.toString(), lugar.text.toString(), contacto.text.toString()) }
        root.addView(guardar, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@ReportePerdidaActivity, 16) })
        root.addView(AppUi.secondary(this, "Cancelar").apply { setOnClickListener { finish() } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@ReportePerdidaActivity, 8) })
        root.addView(AppUi.bottomNav(this, { finish() }, { Toast.makeText(this, "Consulta tus casos atendidos desde el panel.", Toast.LENGTH_SHORT).show() }, { Toast.makeText(this, "Elige un reporte desde el panel.", Toast.LENGTH_SHORT).show() }, { Toast.makeText(this, "Perfil del usuario autenticado.", Toast.LENGTH_SHORT).show() }), LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@ReportePerdidaActivity, 22) })
        setContentView(scroll)
        viewModel.mensaje.observe(this) { guardar.isEnabled = true; Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        viewModel.reporte.observe(this) { reporte ->
            startActivity(Intent(this, CoincidenciasActivity::class.java).apply { putExtra("reporteId", reporte.id) })
        }
    }
}
