package com.Jair.practica.view.common

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.model.remote.SesionRemota
import com.Jair.practica.model.remote.SupabaseClient
import java.io.File
import java.util.concurrent.Executors

/** RF operativos del SAE: el gráfico solo contiene estados excluyentes de objetos. */
class ReportesActivity : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var estado: TextView
    private lateinit var contenido: LinearLayout
    private var indicadores: IndicadoresOperativos? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SesionRemota.rol != "ADMINISTRADOR_SAE") { finish(); return }
        val (scroll, root) = AppUi.screen(this)
        root.id = R.id.rootReportes
        root.addView(AppUi.header(this, "Reportes operativos", "Indicadores del SAE") { finish() })
        estado = TextView(this).apply { text = "Consultando Supabase…"; textSize = 12f; setTextColor(AppUi.MUTED) }
        root.addView(estado)
        contenido = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(contenido)
        root.addView(AppUi.secondary(this, "Actualizar indicadores").apply {
            setOnClickListener { cargar() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@ReportesActivity, 12) })
        root.addView(AppUi.primary(this, "Exportar resumen CSV").apply {
            id = R.id.btnExportarCsv
            setOnClickListener { exportar() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@ReportesActivity, 10) })
        root.addView(AppUi.bottomNav(this, { finish() }, { finish() },
            { android.widget.Toast.makeText(this, "Ya estás en Reportes.", android.widget.Toast.LENGTH_SHORT).show() },
            { startActivity(android.content.Intent(this, PerfilActivity::class.java).apply {
                putExtra("rol", intent.getStringExtra("rol"))
            }) }), LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@ReportesActivity, 18) })
        setContentView(scroll)
        cargar()
    }

    private fun cargar() {
        estado.text = "Consultando Supabase…"
        executor.execute {
            val resultado = runCatching {
                val objetos = SupabaseClient.api.consultarObjetos().execute()
                val perdidas = SupabaseClient.casos.consultarReportes().execute()
                val reclamaciones = SupabaseClient.casos.consultarReclamaciones().execute()
                val entregas = SupabaseClient.casos.consultarEntregas().execute()
                check(objetos.isSuccessful && perdidas.isSuccessful && reclamaciones.isSuccessful && entregas.isSuccessful) {
                    "No se pudieron consultar todos los casos. Objetos ${objetos.code()}, pérdidas ${perdidas.code()}, reclamaciones ${reclamaciones.code()}, entregas ${entregas.code()}"
                }
                IndicadoresOperativos.calcular(
                    objetos.body().orEmpty().map { it.estado },
                    perdidas.body().orEmpty().size,
                    reclamaciones.body().orEmpty().map { it.decision },
                    entregas.body().orEmpty().map { it.codigo_entrega }
                )
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                resultado.onSuccess {
                    indicadores = it
                    estado.text = "Indicadores actualizados desde Supabase"
                    mostrar(it)
                }.onFailure { estado.text = "Sin datos actualizados: ${it.message}" }
            }
        }
    }

    private fun mostrar(datos: IndicadoresOperativos) {
        contenido.removeAllViews()
        val segmentos = listOf(
            GraficoCircularView.Segmento("Pendientes", datos.pendientesRecepcion, AppUi.AMBER),
            GraficoCircularView.Segmento("En custodia", datos.enCustodia, AppUi.RED),
            GraficoCircularView.Segmento("Entregados", datos.entregados, AppUi.GREEN),
            GraficoCircularView.Segmento("Anulados", datos.anulados, AppUi.MUTED)
        )
        contenido.addView(AppUi.card(this, 14).apply {
            addView(TextView(this@ReportesActivity).apply {
                text = "Distribución de objetos encontrados"
                textSize = 16f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(AppUi.INK)
            })
            addView(GraficoCircularView(this@ReportesActivity).apply {
                this.segmentos = segmentos
            }, LinearLayout.LayoutParams(-1, AppUi.dp(this@ReportesActivity, 350)))
        })
        listOf(
            datos.totalObjetos to "Objetos encontrados",
            datos.totalPerdidas to "Reportes de pérdida",
            datos.reclamacionesPendientes to "Reclamaciones pendientes",
            datos.reclamacionesAprobadas to "Reclamaciones aprobadas",
            datos.totalEntregas to "Entregas confirmadas"
        ).forEach { (valor, etiqueta) ->
            contenido.addView(AppUi.card(this, 10).apply {
                addView(TextView(this@ReportesActivity).apply {
                    text = valor.toString(); textSize = 25f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    setTextColor(AppUi.RED)
                })
                addView(TextView(this@ReportesActivity).apply {
                    text = etiqueta; textSize = 13f; setTextColor(AppUi.INK)
                })
            })
        }
    }

    private fun exportar() {
        val datos = indicadores ?: run {
            android.widget.Toast.makeText(this, "Actualiza los indicadores antes de exportar.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val file = File(getExternalFilesDir("reportes"), "resumen-operativo.csv")
        file.parentFile?.mkdirs()
        file.writeText("indicador,valor\n" + listOf(
            "objetos_encontrados" to datos.totalObjetos,
            "pendientes_recepcion" to datos.pendientesRecepcion,
            "en_custodia" to datos.enCustodia,
            "entregados" to datos.entregados,
            "anulados" to datos.anulados,
            "reportes_perdida" to datos.totalPerdidas,
            "reclamaciones_pendientes" to datos.reclamacionesPendientes,
            "reclamaciones_aprobadas" to datos.reclamacionesAprobadas,
            "entregas_confirmadas" to datos.totalEntregas
        ).joinToString("\n") { "${it.first},${it.second}" })
        android.widget.Toast.makeText(this, "CSV exportado: ${file.name}", android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}

data class IndicadoresOperativos(
    val pendientesRecepcion: Int,
    val enCustodia: Int,
    val entregados: Int,
    val anulados: Int,
    val totalPerdidas: Int,
    val reclamacionesPendientes: Int,
    val reclamacionesAprobadas: Int,
    val totalEntregas: Int
) {
    val totalObjetos: Int get() = pendientesRecepcion + enCustodia + entregados + anulados

    companion object {
        fun calcular(objetos: List<String>, perdidas: Int, reclamaciones: List<String>, entregas: List<String>) =
            IndicadoresOperativos(
                pendientesRecepcion = objetos.count { it == "HALLAZGO_REPORTADO" || it == "PENDIENTE_RECEPCION" },
                enCustodia = objetos.count { it == "BAJO_CUSTODIA" || it == "RECLAMADO" },
                entregados = objetos.count { it == "ENTREGADO" },
                anulados = objetos.count { it == "ANULADO" },
                totalPerdidas = perdidas,
                reclamacionesPendientes = reclamaciones.count { it == "PENDIENTE_VERIFICACION" },
                reclamacionesAprobadas = reclamaciones.count { it == "APROBADA" },
                totalEntregas = entregas.distinct().size
            )
    }
}
