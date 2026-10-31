package com.Jair.practica.view.custodia

import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.view.devolucion.HistorialEntregasActivity
import com.Jair.practica.view.devolucion.VerificacionEntregaActivity
import com.Jair.practica.view.devolucion.VerificarReclamacionActivity

class InventarioController(private val activity: AppCompatActivity) {

    fun iniciar() {
        val inventario = 142
        val reclamados = 89
        val vencen = 28

        activity.findViewById<TextView>(R.id.tvInventarioValor).text = inventario.toString()
        activity.findViewById<TextView>(R.id.tvReclamadosValor).text = reclamados.toString()
        activity.findViewById<TextView>(R.id.tvVencenValor).text = vencen.toString()

        val botones = listOf(
            R.id.btnVerFicha1, R.id.btnVerFicha2, R.id.btnVerFicha3
        )
        botones.forEach { id ->
            activity.findViewById<Button>(id).setOnClickListener {
                activity.startActivity(Intent(activity, ProtocoloEntregaActivity::class.java))
            }
        }

        activity.findViewById<android.view.View>(R.id.navCustodia).setOnClickListener {
            activity.startActivity(Intent(activity, HistorialEntregasActivity::class.java))
        }

        listOf(R.id.btnEntregar1, R.id.btnEntregar2, R.id.btnEntregar3).forEach { id ->
            activity.findViewById<Button>(id).setOnClickListener {
                activity.startActivity(Intent(activity, VerificacionEntregaActivity::class.java))
            }
        }
        activity.findViewById<android.view.View>(R.id.navReportes).setOnClickListener {
            activity.startActivity(Intent(activity, HistorialEntregasActivity::class.java))
        }

        activity.findViewById<Button>(R.id.btnVerReclamaciones).setOnClickListener {
            activity.startActivity(
                Intent(activity, VerificarReclamacionActivity::class.java).apply {
                    putExtra("usuarioId", LocalDataStore.USUARIO_ADMINISTRADOR_ID)
                    putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
                    putExtra(VerificarReclamacionActivity.EXTRA_RECLAMACION, LocalDataStore.RECLAMACION_DEMO_ID)
                }
            )
        }

        resaltarNavItem(R.id.navObjetos)
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
            activity.findViewById<ImageView>(ids.first).setColorFilter(
                android.graphics.Color.parseColor(color)
            )
            activity.findViewById<TextView>(ids.second).setTextColor(
                android.graphics.Color.parseColor(color)
            )
        }
    }
}
