package com.Jair.practica.eleccion_categoria

import android.content.Intent
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.view.login.LoginActivity

class MainController(private val activity: AppCompatActivity) {

    fun iniciar() {
        val grupoRol = activity.findViewById<RadioGroup>(R.id.rgRol)

        activity.findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            val idSeleccionado = grupoRol.checkedRadioButtonId
            if (idSeleccionado == -1) {
                Toast.makeText(activity, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rol = if (idSeleccionado == R.id.rbHallador) ROL_HALLADOR else ROL_SERVICIO
            activity.startActivity(
                Intent(activity, LoginActivity::class.java).putExtra(EXTRA_ROL, rol)
            )
        }
    }

    companion object {
        const val EXTRA_ROL = "rol"
        const val ROL_HALLADOR = "hallador"
        const val ROL_SERVICIO = "servicio"
    }
}
