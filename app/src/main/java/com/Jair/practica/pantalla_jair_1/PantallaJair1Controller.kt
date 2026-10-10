package com.Jair.practica.pantalla_jair_1

import android.content.Intent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.pantalla_jair_2.PantallaJair2Activity
import com.Jair.practica.pantalla_jair_3.PantallaJair3Activity

class PantallaJair1Controller(private val activity: AppCompatActivity) {

    fun iniciar() {
        val inventario = 142
        val reclamados = 89
        val vencen = 28

        activity.findViewById<TextView>(R.id.tvInventarioValor).text = inventario.toString()
        activity.findViewById<TextView>(R.id.tvReclamadosValor).text = reclamados.toString()
        activity.findViewById<TextView>(R.id.tvVencenValor).text = vencen.toString()

        val botones = listOf(
            R.id.btnVerFicha1, R.id.btnVerFicha2, R.id.btnVerFicha3,
            R.id.btnEntregar1, R.id.btnEntregar2, R.id.btnEntregar3
        )
        botones.forEach { id ->
            activity.findViewById<Button>(id).setOnClickListener {
                activity.startActivity(Intent(activity, PantallaJair2Activity::class.java))
            }
        }

        activity.findViewById<android.view.View>(R.id.navCustodia).setOnClickListener {
            activity.startActivity(Intent(activity, PantallaJair3Activity::class.java))
        }
    }
}
