package com.Jair.practica.view.perdida

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.viewmodel.perdida.ReportePerdidaViewModel

/** RF-05/RF-06: documentar una pérdida y consultar sugerencias de coincidencia. */
class ReportePerdidaActivity : AppCompatActivity() {
    private val viewModel: ReportePerdidaViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }
        fun campo(hint: String) = EditText(this).apply { this.hint = hint; setPadding(12, 12, 12, 12) }
        root.addView(TextView(this).apply { text = "Reportar pérdida"; textSize = 24f; setTextColor(Color.rgb(147,0,41)) })
        val descripcion = campo("Descripción del objeto perdido")
        val lugar = campo("Pabellón, piso o aula (opcional)")
        val contacto = campo("Correo o teléfono de contacto")
        root.addView(descripcion, lp()); root.addView(lugar, lp()); root.addView(contacto, lp())
        val guardar = Button(this).apply { text = "Guardar reporte y buscar coincidencias" }
        root.addView(guardar, lp())
        setContentView(root)
        val rol = LoginRole.fromCode(intent.getStringExtra("rol") ?: LoginRole.PERSONAL_REGISTRADOR.code)
        val usuario = intent.getStringExtra("usuarioId") ?: "usr-registrador-01"
        guardar.setOnClickListener { guardar.isEnabled = false; viewModel.registrar(rol, usuario, descripcion.text.toString(), lugar.text.toString(), contacto.text.toString()) }
        viewModel.mensaje.observe(this) { guardar.isEnabled = true; Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        viewModel.reporte.observe(this) { reporte ->
            startActivity(Intent(this, CoincidenciasActivity::class.java).apply { putExtra("reporteId", reporte.id) })
        }
    }
    private fun lp() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 18 }
}
