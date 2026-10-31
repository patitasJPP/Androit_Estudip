package com.Jair.practica.view.common

import android.os.Bundle
import android.graphics.Color
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SeguimientoAvisosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }
        root.addView(TextView(this).apply { text = "Seguimiento y avisos"; textSize = 24f; setTextColor(Color.rgb(147, 0, 41)) })
        root.addView(TextView(this).apply {
            text = "Vista demostrativa · estados del usuario autenticado"
            textSize = 13f
            setTextColor(Color.DKGRAY)
        })
        val avisos = listOf(
            "Hallazgo pendiente de recepción" to "Objeto celular · SAE debe confirmar custodia · Pendiente",
            "Reporte de pérdida en búsqueda" to "Laptop negra · Registrado hoy · En seguimiento",
            "Coincidencia sugerida" to "Código UTP-2026-0142 · Requiere verificación del SAE",
            "Reclamación pendiente de verificación" to "Reclamación REC-2026-0001 · Pendiente",
            "Entrega confirmada" to "Entrega ENT-2026-0001 · Registrada el 30/10/2026"
        )
        avisos.forEach { (titulo, detalle) ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(18, 14, 18, 14)
                setBackgroundColor(Color.WHITE)
                elevation = 4f
            }
            card.addView(TextView(this).apply { text = titulo; textSize = 16f; setTextColor(Color.rgb(35,35,45)) })
            card.addView(TextView(this).apply { text = detalle; textSize = 13f; setTextColor(Color.DKGRAY) })
            card.setOnClickListener { android.widget.Toast.makeText(this@SeguimientoAvisosActivity, "Aviso seleccionado: $titulo", android.widget.Toast.LENGTH_SHORT).show() }
            root.addView(card, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = 14 })
        }
        root.addView(Button(this).apply { text = "Actualizar demostración"; setOnClickListener { android.widget.Toast.makeText(this@SeguimientoAvisosActivity, "Estados actualizados para la demostración", android.widget.Toast.LENGTH_SHORT).show() } }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = 18 })
        setContentView(root)
    }
}
