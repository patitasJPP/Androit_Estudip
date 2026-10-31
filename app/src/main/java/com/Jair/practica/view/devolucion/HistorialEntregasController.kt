package com.Jair.practica.view.devolucion

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.view.custodia.InventarioActivity

class HistorialEntregasController(private val activity: AppCompatActivity) {

    fun iniciar() {
        wireNav()
        resaltarNavItem(R.id.navCustodia)
    }

    private fun wireNav() {
        activity.findViewById<android.view.View>(R.id.navObjetos).setOnClickListener {
            activity.startActivity(Intent(activity, InventarioActivity::class.java))
        }
        activity.findViewById<android.view.View>(R.id.navCustodia).setOnClickListener {
            // "Resuelto" ya es la pantalla actual.
        }
    }

    private fun resaltarNavItem(itemActivoId: Int) {
        val items = listOf(
            R.id.navObjetos to Pair(R.id.imgNavObjetos, R.id.tvNavObjetos),
            R.id.navCustodia to Pair(R.id.imgNavCustodia, R.id.tvNavCustodia),
            R.id.navReportes to Pair(R.id.imgNavReportes, R.id.tvNavReportes),
            R.id.navPerfil to Pair(R.id.imgNavPerfil, R.id.tvNavPerfil)
        )
        items.forEach { (itemId, ids) ->
            val color = if (itemId == itemActivoId) "#930029" else "#1B1B25"
            activity.findViewById<ImageView>(ids.first).setColorFilter(Color.parseColor(color))
            activity.findViewById<TextView>(ids.second).setTextColor(Color.parseColor(color))
        }
    }
}
