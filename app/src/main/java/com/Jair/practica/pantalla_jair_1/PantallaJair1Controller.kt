package com.Jair.practica.pantalla_jair_1

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class PantallaJair1Controller(private val activity: AppCompatActivity) {

    fun iniciar() {
        val inventario = 142
        val reclamados = 89
        val vencen = 28

        activity.findViewById<TextView>(R.id.tvInventarioValor).text = inventario.toString()
        activity.findViewById<TextView>(R.id.tvReclamadosValor).text = reclamados.toString()
        activity.findViewById<TextView>(R.id.tvVencenValor).text = vencen.toString()
    }
}
