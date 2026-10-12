package com.Jair.practica.login

import android.content.Intent
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.eleccion_categoria.MainController
import com.Jair.practica.pantalla_hallazgo.PantallaHallazgoActivity
import com.Jair.practica.pantalla_jair_1.PantallaJair1Activity

class LoginController(private val activity: AppCompatActivity) {

    // Credenciales demo. Índice 0 = Hallador, índice 1 = Servicio.
    private val usuarios = arrayOf("jair", "jair")
    private val contrasenas = arrayOf("jair", "jair123")

    fun iniciar() {
        val etUsuario = activity.findViewById<EditText>(R.id.etUsuario)
        val etContrasena = activity.findViewById<EditText>(R.id.etContrasena)

        activity.findViewById<ImageView>(R.id.imgMostrarClave).setOnClickListener {
            val visible = etContrasena.inputType ==
                (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            etContrasena.inputType = if (visible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            etContrasena.setSelection(etContrasena.text.length)
        }

        activity.findViewById<android.view.View>(R.id.btnLogin).setOnClickListener {
            val rol = activity.intent.getStringExtra(MainController.EXTRA_ROL)
                ?: MainController.ROL_HALLADOR
            val indice = if (rol == MainController.ROL_SERVICIO) 1 else 0

            val usuarioOk = etUsuario.text.toString().trim() == usuarios[indice]
            val contrasenaOk = etContrasena.text.toString() == contrasenas[indice]

            if (usuarioOk && contrasenaOk) {
                val destino = if (indice == 0) {
                    PantallaHallazgoActivity::class.java
                } else {
                    PantallaJair1Activity::class.java
                }
                activity.startActivity(Intent(activity, destino))
                activity.finish()
            } else {
                Toast.makeText(activity, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
