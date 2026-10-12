package com.Jair.practica.pantalla_jair_3

import android.content.Intent
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.pantalla_jair_1.PantallaJair1Activity
import com.Jair.practica.pantalla_noe_2.PantallaNoe2Activity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PantallaJair3Controller(private val activity: AppCompatActivity) {

    fun iniciar() {
        wireBotonesActa()
        wireChips()
        wireNav()
        resaltarNavItem(R.id.navCustodia)
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

    private fun wireBotonesActa() {
        val ids = listOf(
            R.id.btnVerActa1, R.id.btnFicha1,
            R.id.btnVerActa2, R.id.btnFicha2,
            R.id.btnVerActa3, R.id.btnFicha3
        )
        ids.forEach { id ->
            activity.findViewById<MaterialCardView>(id).setOnClickListener {
                activity.startActivity(Intent(activity, PantallaNoe2Activity::class.java))
            }
        }
    }

    private fun wireChips() {
        val grupo = activity.findViewById<ChipGroup>(R.id.grupoFiltrosHistorial)
        grupo.setOnCheckedStateChangeListener { _, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = activity.findViewById<Chip>(id)
            Toast.makeText(
                activity,
                activity.getString(R.string.filtro_demo_seleccionado, chip.text),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun wireNav() {
        activity.findViewById<android.view.View>(R.id.navObjetos).setOnClickListener {
            activity.startActivity(Intent(activity, PantallaJair1Activity::class.java))
        }
        activity.findViewById<android.view.View>(R.id.navCustodia).setOnClickListener {
            // "Resuelto" ya es la pantalla actual.
        }
    }
}
