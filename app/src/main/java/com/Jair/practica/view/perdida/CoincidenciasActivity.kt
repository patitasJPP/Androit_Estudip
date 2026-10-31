package com.Jair.practica.view.perdida

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.model.local.LocalReportePerdidaRepository
import com.Jair.practica.viewmodel.perdida.ReportePerdidaViewModel

class CoincidenciasActivity : AppCompatActivity() {
    private val viewModel: ReportePerdidaViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }
        root.addView(TextView(this).apply { text = "Posibles coincidencias"; textSize = 24f })
        val contenido = TextView(this).apply { text = "Consultando reportes..."; textSize = 16f }
        root.addView(contenido)
        setContentView(root)
        val reporte = LocalReportePerdidaRepository().findById(intent.getStringExtra("reporteId").orEmpty())
        if (reporte == null) { contenido.text = "No se encontró el reporte."; return }
        viewModel.buscar(reporte)
        viewModel.coincidencias.observe(this) { lista ->
            contenido.text = if (lista.isEmpty()) "No hay coincidencias por ahora. El reporte queda en seguimiento." else
                lista.joinToString("\n\n") { "Código: ${it.codigoCustodia}\n${it.categoria}: ${it.descripcion}\nLugar: ${it.lugarHallazgo}\nEstado: ${it.estado}\n\nLa sugerencia no autoriza una devolución." }
        }
    }
}
