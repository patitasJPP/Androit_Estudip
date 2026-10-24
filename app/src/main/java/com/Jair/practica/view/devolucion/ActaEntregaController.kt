package com.Jair.practica.view.devolucion

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.view.common.NavegacionPrincipal

class ActaEntregaController(private val activity: AppCompatActivity) {
    fun iniciar() {
        activity.findViewById<View>(R.id.btnPanelCustodia).setOnClickListener {
            NavegacionPrincipal.panel(activity)
        }
        activity.findViewById<View>(R.id.btnDescargarActa).setOnClickListener {
            NavegacionPrincipal.aviso(activity, "Vista de ejemplo: descarga de PDF todavía no disponible")
        }
        activity.findViewById<View>(R.id.btnReenviarCorreo).setOnClickListener {
            NavegacionPrincipal.aviso(activity, "Vista de ejemplo: envío de correo todavía no disponible")
        }
    }
}
