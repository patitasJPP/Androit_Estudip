package com.Jair.practica.pantalla_jair_2

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.google.android.material.card.MaterialCardView

class PantallaJair2Controller(private val activity: AppCompatActivity) {

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