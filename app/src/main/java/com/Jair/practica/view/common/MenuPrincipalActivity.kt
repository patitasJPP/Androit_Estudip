package com.Jair.practica.view.common

import android.content.Intent
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.LocalReclamacionRepository
import com.Jair.practica.model.remote.SincronizacionCasos
import com.Jair.practica.model.remote.SincronizacionRemota
import com.Jair.practica.view.custodia.InventarioActivity
import com.Jair.practica.view.devolucion.HistorialEntregasActivity
import com.Jair.practica.view.devolucion.VerificarReclamacionActivity
import com.Jair.practica.view.hallazgo.ConfirmarRecepcionActivity
import com.Jair.practica.view.hallazgo.RegistroHallazgoActivity
import com.Jair.practica.view.perdida.ReportePerdidaActivity

/** Paneles principales por rol. El menú orienta el flujo y evita una lista plana de botones. */
class MenuPrincipalActivity : AppCompatActivity() {
    private lateinit var rol: LoginRole
    private val usuarioId get() = intent.getStringExtra("usuarioId").orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rol = LoginRole.fromCode(intent.getStringExtra("rol") ?: LoginRole.PERSONAL_REGISTRADOR.code)
        mostrarPanel()
    }

    override fun onResume() {
        super.onResume()
        SincronizacionCasos.sincronizarPendientes()
        SincronizacionRemota.refrescarObjetos {
            SincronizacionCasos.refrescarCasos {
                if (!isFinishing && !isDestroyed) mostrarPanel()
            }
        }
    }

    private fun mostrarPanel() {
        val (scroll, root) = AppUi.screen(this)
        val esAdmin = rol == LoginRole.ADMINISTRADOR_SAE
        root.addView(AppUi.header(this, if (esAdmin) "Panel SAE" else "Panel registrador", "UTP+objects"))
        root.addView(TextView(this).apply { text = if (esAdmin) "Administrador SAE · sesión activa" else "Personal registrador · sesión activa"; textSize = 12f; setTextColor(AppUi.MUTED) })

        if (!esAdmin) {
            val welcome = AppUi.card(this, 14)
            welcome.addView(TextView(this).apply { text = "Gestión de casos"; textSize = 17f; typeface = Typeface.DEFAULT_BOLD; setTextColor(AppUi.INK) })
            welcome.addView(TextView(this).apply { text = "Registra hallazgos, reporta pérdidas y consulta el seguimiento de tus casos."; textSize = 12f; setTextColor(AppUi.MUTED); setPadding(0, AppUi.dp(this@MenuPrincipalActivity, 6), 0, 0) })
            welcome.addView(AppUi.status(this, "2 ACCIONES DISPONIBLES", AppUi.RED), LinearLayout.LayoutParams(-2, -2).apply { topMargin = AppUi.dp(this@MenuPrincipalActivity, 12) })
            root.addView(welcome)
            accion(root, "Documentar hallazgo", "Registrar características, lugar y evidencia del objeto encontrado.", "Iniciar registro") { abrir(RegistroHallazgoActivity::class.java) }
            accion(root, "Reportar o reanudar pérdida", "Conservar la descripción y el contacto para buscar coincidencias.", "Crear reporte") { abrir(ReportePerdidaActivity::class.java) }
            accion(root, "Seguimiento de mis casos", "Consulta recepción, coincidencias, reclamaciones y entregas vinculadas a tus registros.", "Ver actividad") { abrir(SeguimientoAvisosActivity::class.java) }
        } else {
            val objetos = LocalObjetoEncontradoRepository().list()
            val reclamaciones = LocalReclamacionRepository().listPendientes(LoginRole.ADMINISTRADOR_SAE)
            val enCustodia = objetos.count { it.estado == EstadoObjeto.BAJO_CUSTODIA }
            val pendientes = objetos.count {
                it.estado == EstadoObjeto.HALLAZGO_REPORTADO || it.estado == EstadoObjeto.PENDIENTE_RECEPCION
            } + reclamaciones.count { it.estado == EstadoReclamacion.PENDIENTE_VERIFICACION }
            val entregados = objetos.count { it.estado == EstadoObjeto.ENTREGADO }
            val summary = AppUi.card(this, 14)
            summary.addView(TextView(this).apply { text = "Resumen operativo"; textSize = 16f; typeface = Typeface.DEFAULT_BOLD; setTextColor(AppUi.INK) })
            val metrics = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, AppUi.dp(this@MenuPrincipalActivity, 14), 0, 0) }
            listOf(enCustodia.toString() to "En custodia", pendientes.toString() to "Pendientes", entregados.toString() to "Entregados").forEach { (value, label) ->
                val metric = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
                metric.addView(TextView(this).apply { text = value; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; setTextColor(AppUi.RED); gravity = Gravity.CENTER })
                metric.addView(TextView(this).apply { text = label; textSize = 10f; setTextColor(AppUi.MUTED); gravity = Gravity.CENTER })
                metrics.addView(metric, LinearLayout.LayoutParams(0, -2, 1f))
            }
            summary.addView(metrics); root.addView(summary)
            accion(root, "Consultar inventario", "Buscar objetos bajo custodia por código, categoría o ubicación.", "Abrir inventario") { abrir(InventarioActivity::class.java) }
            accion(root, "Recepciones pendientes", "Elige entre todos los hallazgos pendientes y confirma solo el objeto recibido físicamente.", "Ver hallazgos") { abrirPendientes(PendientesSaeActivity.TIPO_RECEPCIONES) }
            accion(root, "Reclamaciones por atender", "Elige una reclamación pendiente para verificarla o continuar una entrega aprobada.", "Ver reclamaciones") { abrirPendientes(PendientesSaeActivity.TIPO_RECLAMACIONES) }
            accion(root, "Historial de entregas", "Consulta actas, receptores y entregas confirmadas.", "Abrir historial") { abrir(HistorialEntregasActivity::class.java) }
            accion(root, "Administrar accesos", "Habilita, deshabilita o restablece cuentas de registradores.", "Gestionar accesos") { abrir(AdministrarAccesosActivity::class.java) }
            accion(root, "Seguimiento y avisos", "Revisa pendientes de recepción, reclamaciones y entregas.", "Ver actividad") { abrir(SeguimientoAvisosActivity::class.java) }
            accion(root, "Reportes operativos", "Consulta indicadores actualizados y exporta el resumen del SAE.", "Abrir reportes") { abrir(ReportesActivity::class.java) }
            accion(root, "Mi perfil", "Consulta tu rol, campus y estado de acceso institucional.", "Abrir perfil") { abrir(PerfilActivity::class.java) }
        }
        root.addView(AppUi.bottomNav(this,
            { Toast.makeText(this, "Estás en el panel principal.", Toast.LENGTH_SHORT).show() },
            { if (esAdmin) abrir(HistorialEntregasActivity::class.java) else abrir(SeguimientoAvisosActivity::class.java) },
            { abrir(ReportesActivity::class.java) },
            { abrir(PerfilActivity::class.java) }
        ), LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@MenuPrincipalActivity, 18) })
        setContentView(scroll)
    }

    private fun accion(root: LinearLayout, titulo: String, descripcion: String, boton: String, action: () -> Unit) {
        val card = AppUi.card(this, 10)
        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(TextView(this).apply { text = titulo; textSize = 15f; typeface = Typeface.DEFAULT_BOLD; setTextColor(AppUi.INK) }, LinearLayout.LayoutParams(0, -2, 1f))
        head.addView(AppUi.status(this, "DISPONIBLE", AppUi.GREEN)); card.addView(head)
        card.addView(TextView(this).apply { text = descripcion; textSize = 12f; setTextColor(AppUi.MUTED); setPadding(0, AppUi.dp(this@MenuPrincipalActivity, 6), 0, 0) })
        card.addView(AppUi.primary(this, boton).apply { setOnClickListener { action() } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@MenuPrincipalActivity, 11) })
        root.addView(card)
    }

    private fun abrir(destino: Class<out AppCompatActivity>) = startActivity(Intent(this, destino).apply { putExtra("usuarioId", usuarioId); putExtra("rol", rol.code) })

    private fun abrirPendientes(tipo: String) = startActivity(Intent(this, PendientesSaeActivity::class.java).apply {
        putExtra("usuarioId", usuarioId)
        putExtra("rol", rol.code)
        putExtra(PendientesSaeActivity.EXTRA_TIPO, tipo)
    })
}
