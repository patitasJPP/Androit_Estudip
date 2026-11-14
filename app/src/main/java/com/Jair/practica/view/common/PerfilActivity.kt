package com.Jair.practica.view.common

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.remote.SesionRemota
import com.Jair.practica.view.login.LoginActivity

class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val (scroll, root) = AppUi.screen(this)
        root.id = R.id.rootPerfil
        val rol = LoginRole.fromCode(intent.getStringExtra("rol") ?: LoginRole.PERSONAL_REGISTRADOR.code)
        val usuarioId = SesionRemota.usuarioId.orEmpty()
        root.addView(AppUi.header(this, "Mi perfil", "Cuenta institucional") { finish() })
        val card = AppUi.card(this, 14)
        card.addView(TextView(this).apply { text = "Sesión autenticada"; textSize = 17f; typeface = android.graphics.Typeface.DEFAULT_BOLD; setTextColor(AppUi.INK) })
        listOf("Nombre" to SesionRemota.nombre.orEmpty(), "Correo" to SesionRemota.correo.orEmpty(), "Identificador" to usuarioId, "Rol" to if (rol == LoginRole.ADMINISTRADOR_SAE) "Administrador SAE" else "Personal registrador", "Campus" to "Lima Este – Ate").forEach { (label, value) ->
            card.addView(TextView(this).apply { text = "$label\n$value"; textSize = 13f; setTextColor(AppUi.INK); setPadding(0, AppUi.dp(this@PerfilActivity, 10), 0, 0) })
        }
        root.addView(card)
        root.addView(AppUi.secondary(this, "Cerrar sesión").apply { setOnClickListener {
            SesionRemota.cerrar()
            startActivity(android.content.Intent(this@PerfilActivity, LoginActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        } }, android.widget.LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@PerfilActivity, 16) })
        root.addView(AppUi.bottomNav(this, { finish() }, { abrirSeguimiento() }, { abrirReportes() }, { android.widget.Toast.makeText(this, "Ya estás en Mi perfil.", android.widget.Toast.LENGTH_SHORT).show() }) , android.widget.LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@PerfilActivity, 18) })
        setContentView(scroll)
    }

    private fun abrirReportes() = startActivity(android.content.Intent(this, ReportesActivity::class.java).apply {
        putExtra("usuarioId", intent.getStringExtra("usuarioId")); putExtra("rol", intent.getStringExtra("rol"))
    })

    private fun abrirSeguimiento() = startActivity(android.content.Intent(this, SeguimientoAvisosActivity::class.java).apply {
        putExtra("usuarioId", intent.getStringExtra("usuarioId")); putExtra("rol", intent.getStringExtra("rol"))
    })
}
