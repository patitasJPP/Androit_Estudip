package com.Jair.practica.view.custodia

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.google.android.material.card.MaterialCardView

class ProtocoloEntregaController(private val activity: AppCompatActivity) {

    fun iniciar() {
        activity.findViewById<MaterialCardView>(R.id.btnCerrarX).setOnClickListener {
            activity.finish()
        }

        activity.findViewById<MaterialCardView>(R.id.btnCerrarVentana).setOnClickListener {
            activity.finish()
        }

        activity.findViewById<MaterialCardView>(R.id.btnAceptarProtocolo).setOnClickListener {
            Toast.makeText(activity, "Protocolo aceptado", Toast.LENGTH_SHORT).show()
        }
    }
}