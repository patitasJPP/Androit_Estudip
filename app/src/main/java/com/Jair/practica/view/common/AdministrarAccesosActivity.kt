package com.Jair.practica.view.common

import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.model.remote.SesionRemota
import com.Jair.practica.model.remote.SupabaseAuthClient
import com.Jair.practica.model.remote.SupabaseClient
import com.Jair.practica.model.remote.UsuarioOperativoRemoto
import java.util.concurrent.Executors

/** RF-11: el SAE administra cuentas existentes, sin cambiar sus roles. */
class AdministrarAccesosActivity : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var contenido: LinearLayout
    private lateinit var estado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SesionRemota.rol != "ADMINISTRADOR_SAE") { finish(); return }
        val (scroll, root) = AppUi.screen(this)
        root.addView(AppUi.header(this, "Administrar accesos", "Seguridad y usuarios") { finish() })
        root.addView(AppUi.card(this, 14).apply {
            addView(TextView(this@AdministrarAccesosActivity).apply {
                text = "Cuentas del Personal registrador"
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(AppUi.INK)
            })
            addView(TextView(this@AdministrarAccesosActivity).apply {
                text = "Consulta el estado de cada cuenta. El cambio de acceso no modifica el rol del trabajador."
                textSize = 12f
                setTextColor(AppUi.MUTED)
            })
        })
        estado = TextView(this).apply { text = "Cargando cuentas…"; textSize = 12f; setTextColor(AppUi.MUTED) }
        root.addView(estado)
        contenido = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(contenido)
        root.addView(AppUi.secondary(this, "Actualizar cuentas").apply {
            setOnClickListener { cargar() }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@AdministrarAccesosActivity, 12) })
        setContentView(scroll)
        cargar()
    }

    private fun cargar() {
        estado.text = "Actualizando cuentas…"
        executor.execute {
            val resultado = runCatching {
                val respuesta = SupabaseClient.usuarios.consultar().execute()
                check(respuesta.isSuccessful) { "Consulta HTTP ${respuesta.code()}" }
                respuesta.body().orEmpty().sortedBy { it.nombre_completo }
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                contenido.removeAllViews()
                resultado.onSuccess { lista ->
                    estado.text = "${lista.size} cuenta(s) de Personal registrador"
                    if (lista.isEmpty()) estado.text = "No hay cuentas de registradores."
                    lista.forEach(::mostrarCuenta)
                }.onFailure { estado.text = "No se pudieron cargar las cuentas: ${it.message}" }
            }
        }
    }

    private fun mostrarCuenta(usuario: UsuarioOperativoRemoto) {
        val card = AppUi.card(this, 12)
        card.addView(TextView(this).apply {
            text = usuario.nombre_completo
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AppUi.INK)
        })
        card.addView(TextView(this).apply {
            text = usuario.correo.orEmpty()
            textSize = 12f
            setTextColor(AppUi.MUTED)
        })
        card.addView(AppUi.status(this,
            if (usuario.activo) "ACCESO HABILITADO" else "ACCESO DESHABILITADO",
            if (usuario.activo) AppUi.GREEN else AppUi.RED))
        card.addView(AppUi.primary(this, if (usuario.activo) "Deshabilitar acceso" else "Habilitar acceso").apply {
            setOnClickListener { confirmarCambio(usuario) }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@AdministrarAccesosActivity, 12) })
        card.addView(AppUi.secondary(this, "Enviar enlace para restablecer contraseña").apply {
            setOnClickListener { confirmarRecuperacion(usuario) }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = AppUi.dp(this@AdministrarAccesosActivity, 8) })
        contenido.addView(card)
    }

    private fun confirmarCambio(usuario: UsuarioOperativoRemoto) {
        val accion = if (usuario.activo) "deshabilitar" else "habilitar"
        AlertDialog.Builder(this)
            .setTitle("Confirmar cambio de acceso")
            .setMessage("¿Deseas $accion a ${usuario.nombre_completo}? El rol permanecerá igual.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ -> cambiarEstado(usuario, !usuario.activo) }
            .show()
    }

    private fun cambiarEstado(usuario: UsuarioOperativoRemoto, activo: Boolean) {
        estado.text = "Guardando cambio de acceso…"
        executor.execute {
            val resultado = runCatching {
                val respuesta = SupabaseClient.usuarios.cambiarEstado("eq.${usuario.id}", mapOf("activo" to activo)).execute()
                check(respuesta.isSuccessful && respuesta.body()?.singleOrNull()?.activo == activo) {
                    "No se confirmó el cambio en Supabase: HTTP ${respuesta.code()}"
                }
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                resultado.onSuccess { cargar() }
                    .onFailure { estado.text = "No se cambió el acceso: ${it.message}" }
            }
        }
    }

    private fun confirmarRecuperacion(usuario: UsuarioOperativoRemoto) {
        val correo = usuario.correo?.takeIf { it.contains('@') }
        if (correo == null) { estado.text = "Esta cuenta no tiene un correo válido."; return }
        AlertDialog.Builder(this)
            .setTitle("Restablecer contraseña")
            .setMessage("Se enviará un enlace de recuperación a $correo. ¿Continuar?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Enviar enlace") { _, _ -> enviarRecuperacion(correo) }
            .show()
    }

    private fun enviarRecuperacion(correo: String) {
        estado.text = "Solicitando enlace de recuperación…"
        executor.execute {
            val resultado = runCatching {
                val respuesta = SupabaseAuthClient.api.recuperarClave(mapOf("email" to correo)).execute()
                check(respuesta.isSuccessful) { "Supabase HTTP ${respuesta.code()}" }
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                estado.text = resultado.fold(
                    onSuccess = { "Solicitud enviada a $correo. Revisa el correo y sigue el enlace." },
                    onFailure = { "No se pudo solicitar el restablecimiento: ${it.message}" }
                )
            }
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
