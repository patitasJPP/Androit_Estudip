package com.Jair.practica.view.devolucion

import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.view.common.NavegacionPrincipal

class VerificacionEntregaController(private val activity: AppCompatActivity) {
    fun iniciar() {
        configurarResolucion()
        configurarDocumento()
        activity.findViewById<View>(R.id.cardVolver).setOnClickListener {
            NavegacionPrincipal.panel(activity)
        }
        activity.findViewById<View>(R.id.btnGenerarReporte).setOnClickListener {
            activity.startActivity(Intent(activity, ActaEntregaActivity::class.java))
        }
        activity.findViewById<View>(R.id.btnConfirmarEntregar).setOnClickListener {
            NavegacionPrincipal.aviso(activity, "Demostración visual: no se ha registrado ninguna entrega")
            val firma = activity.findViewById<View>(R.id.cardFirmaDigital)
            firma.requestRectangleOnScreen(Rect(0, 0, firma.width, firma.height), false)
        }

        val accionesVisuales = mapOf(
            R.id.btnCapturarAnverso to "Captura de documento: disponible en una próxima etapa",
            R.id.btnCapturarReverso to "Captura de documento: disponible en una próxima etapa",
            R.id.btnTomarFotoReclamante to "Captura de entrega: disponible en una próxima etapa",
            R.id.btnValidar to "Datos de ejemplo: validación todavía no implementada",
            R.id.cardAreaFirma to "Firma digital: disponible en una próxima etapa",
            R.id.btnLimpiarTrazo to "El área de firma es una vista de ejemplo"
        )
        accionesVisuales.forEach { (id, mensaje) ->
            activity.findViewById<View>(id).setOnClickListener {
                NavegacionPrincipal.aviso(activity, mensaje)
            }
        }
    }

    private fun configurarResolucion() {
        val grupo = activity.findViewById<RadioGroup>(R.id.rgResolucionEntrega)
        fun actualizarSeleccion(id: Int) {
            val aprobada = id == R.id.rbAprobarEntrega
            activity.findViewById<TextView>(R.id.tvEstadoResolucion).setText(
                if (aprobada) R.string.entrega_resolucion_aprobada else R.string.entrega_resolucion_denegada
            )
            listOf(R.id.btnConfirmarEntregar, R.id.btnGenerarReporte).forEach { botonId ->
                activity.findViewById<View>(botonId).apply {
                    isEnabled = aprobada
                    alpha = if (aprobada) 1f else 0.45f
                }
            }
        }
        grupo.setOnCheckedChangeListener { _, checkedId -> actualizarSeleccion(checkedId) }
        actualizarSeleccion(grupo.checkedRadioButtonId)
    }

    private fun configurarDocumento() {
        val spinner = activity.findViewById<Spinner>(R.id.spinnerTipoDocumento)
        spinner.adapter = ArrayAdapter.createFromResource(
            activity, R.array.entrega_tipos_documento, R.layout.entrega_spinner_item
        ).apply {
            setDropDownViewResource(R.layout.entrega_spinner_item)
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val documento = parent.getItemAtPosition(position).toString()
                activity.findViewById<TextView>(R.id.tvTituloDni).text =
                    activity.getString(R.string.entrega_foto_documento, documento)
                activity.findViewById<TextView>(R.id.chkDniCoincide).text =
                    activity.getString(R.string.entrega_cotejo_documento, documento)
                activity.findViewById<TextView>(R.id.tvAvisoLegibilidad).setText(
                    R.string.entrega_documento_legible
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
    }
}
