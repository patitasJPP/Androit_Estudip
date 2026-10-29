package com.Jair.practica.view.hallazgo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

/** RF-03: confirmación de recepción física y ubicación bajo custodia. */
class ConfirmarRecepcionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmar_recepcion)

        findViewById<TextView>(R.id.tvCodigoRecepcion).text =
            intent.getStringExtra(EXTRA_CODIGO) ?: "UTP-0001"
        findViewById<Button>(R.id.btnConfirmarRecepcion).setOnClickListener {
            findViewById<TextView>(R.id.tvEstadoRecepcion).text =
                "Recepción confirmada · Objeto bajo custodia"
            findViewById<Button>(R.id.btnConfirmarRecepcion).isEnabled = false
        }
        findViewById<Button>(R.id.btnVolverRecepcion).setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_CODIGO = "codigo_hallazgo"
    }
}
