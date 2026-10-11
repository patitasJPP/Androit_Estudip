package com.Jair.practica.pantalla_noe

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.Jair.practica.R
import com.Jair.practica.pantalla_jair_1.PantallaJair1Activity
import com.Jair.practica.pantalla_jair_3.PantallaJair3Activity

/** Navegación del prototipo; no registra entregas ni modifica datos. */
object NavegacionNoe {
    fun configurar(activity: AppCompatActivity, rootId: Int) {
        val root = activity.findViewById<View>(rootId)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val padding = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.setPadding(padding.left, padding.top, padding.right, padding.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)

        activity.findViewById<View>(R.id.navObjetos).setOnClickListener { panel(activity) }
        activity.findViewById<View>(R.id.navCustodia).setOnClickListener { panel(activity) }
        activity.findViewById<View>(R.id.navReportes).setOnClickListener {
            abrir(activity, PantallaJair3Activity::class.java)
        }
        activity.findViewById<View>(R.id.navPerfil).setOnClickListener {
            aviso(activity, "La pantalla de perfil todavía no está disponible")
        }
    }

    fun panel(activity: AppCompatActivity) {
        abrir(activity, PantallaJair1Activity::class.java)
    }

    private fun abrir(activity: AppCompatActivity, destino: Class<out AppCompatActivity>) {
        activity.startActivity(Intent(activity, destino).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        activity.finish()
    }

    fun aviso(activity: AppCompatActivity, mensaje: String) {
        Toast.makeText(activity, mensaje, Toast.LENGTH_SHORT).show()
    }
}
