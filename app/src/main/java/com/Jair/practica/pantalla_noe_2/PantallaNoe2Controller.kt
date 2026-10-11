package com.Jair.practica.pantalla_noe_2

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.pantalla_noe.NavegacionNoe

class PantallaNoe2Controller(private val activity: AppCompatActivity) {
    fun iniciar() {
        activity.findViewById<View>(R.id.btnPanelCustodia).setOnClickListener {
            NavegacionNoe.panel(activity)
        }
        activity.findViewById<View>(R.id.btnDescargarActa).setOnClickListener {
            NavegacionNoe.aviso(activity, "Vista de ejemplo: descarga de PDF todavía no disponible")
        }
        activity.findViewById<View>(R.id.btnReenviarCorreo).setOnClickListener {
            NavegacionNoe.aviso(activity, "Vista de ejemplo: envío de correo todavía no disponible")
        }
    }
}
