package com.Jair.practica.view.login

import android.content.Intent
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class SeleccionRolController(private val activity: AppCompatActivity) {

    fun iniciar() {
        val grupoRol = activity.findViewById<RadioGroup>(R.id.rgRol)

        activity.findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            val idSeleccionado = grupoRol.checkedRadioButtonId
            if (idSeleccionado == -1) {
                Toast.makeText(activity, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rol = when (idSeleccionado) {
                R.id.rbPersonalRegistrador -> ROL_PERSONAL_REGISTRADOR
                R.id.rbAdministradorSae -> ROL_ADMINISTRADOR_SAE
                else -> ROL_PERSONAL_REGISTRADOR
            }

            activity.startActivity(
                Intent(activity, LoginActivity::class.java).putExtra(EXTRA_ROL, rol)
            )
        }
    }

    companion object {
        const val EXTRA_ROL = "rol"
        const val ROL_PERSONAL_REGISTRADOR = "personal_registrador"
        const val ROL_ADMINISTRADOR_SAE = "administrador_sae"
    }
}
