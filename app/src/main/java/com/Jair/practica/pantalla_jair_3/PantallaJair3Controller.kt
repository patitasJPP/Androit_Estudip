package com.Jair.practica.pantalla_jair_3

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class PantallaJair3Controller(private val activity: AppCompatActivity) {

    fun iniciar() {
        activity.findViewById<TextView>(R.id.tvTituloPantalla3).text = "Resuelto"
    }
}