package com.Jair.practica.view.common

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.view.custodia.InventarioActivity
import com.Jair.practica.view.devolucion.HistorialEntregasActivity
import com.Jair.practica.view.devolucion.VerificarReclamacionActivity
import com.Jair.practica.view.hallazgo.ConfirmarRecepcionActivity
import com.Jair.practica.view.hallazgo.RegistroHallazgoActivity
import com.Jair.practica.view.perdida.ReportePerdidaActivity

class MenuPrincipalActivity : AppCompatActivity() {
    private lateinit var rol: LoginRole
    private val usuarioId get() = intent.getStringExtra("usuarioId").orEmpty()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rol = LoginRole.fromCode(intent.getStringExtra("rol") ?: LoginRole.PERSONAL_REGISTRADOR.code)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 40, 32, 32) }
        root.addView(TextView(this).apply { text = "UTP+objects"; textSize = 28f; setTextColor(Color.rgb(147,0,41)) })
        root.addView(TextView(this).apply { text = if (rol == LoginRole.ADMINISTRADOR_SAE) "Panel Administrador SAE" else "Panel Personal registrador"; textSize = 18f })
        if (rol == LoginRole.PERSONAL_REGISTRADOR) {
            boton(root, "Documentar hallazgo") { abrir(RegistroHallazgoActivity::class.java) }
            boton(root, "Registrar o reanudar pérdida") { abrir(ReportePerdidaActivity::class.java) }
        } else {
            boton(root, "Consultar inventario") { abrir(InventarioActivity::class.java) }
            AlmacenLocal.instancia.ultimoObjetoPendienteId?.let { id ->
                boton(root, "Confirmar recepción pendiente") {
                    startActivity(Intent(this, ConfirmarRecepcionActivity::class.java).apply {
                        putExtra("ID_OBJETO_GUARDADO", id); putExtra("rol", rol.code)
                    })
                }
            }
            boton(root, "Verificar reclamación") {
                startActivity(Intent(this, VerificarReclamacionActivity::class.java).apply {
                    putExtra("usuarioId", usuarioId); putExtra("rol", rol.code)
                    putExtra(VerificarReclamacionActivity.EXTRA_RECLAMACION, LocalDataStore.RECLAMACION_DEMO_ID)
                })
            }
            boton(root, "Historial de entregas") { abrir(HistorialEntregasActivity::class.java) }
            boton(root, "Administrar accesos") { abrir(AdministrarAccesosActivity::class.java) }
            boton(root, "Seguimiento y avisos") { abrir(SeguimientoAvisosActivity::class.java) }
        }
        setContentView(root)
    }
    private fun boton(root: LinearLayout, texto: String, accion: () -> Unit) {
        root.addView(Button(this).apply { text = texto; setOnClickListener { accion() } }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 14 })
    }
    private fun abrir(destino: Class<out AppCompatActivity>) = startActivity(Intent(this, destino).apply {
        putExtra("usuarioId", usuarioId); putExtra("rol", rol.code)
    })
}
