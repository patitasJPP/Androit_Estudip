package com.Jair.practica.view.devolucion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.LocalAuditoriaRepository
import com.Jair.practica.model.local.LocalEntregaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.LocalPersonaAtendidaRepository
import com.Jair.practica.model.local.LocalUsuarioRepository
import com.Jair.practica.model.remote.SesionRemota
import com.Jair.practica.model.remote.SupabaseConfig
import com.Jair.practica.view.common.AppUi
import com.Jair.practica.viewmodel.devolucion.ActaEntrega
import com.Jair.practica.viewmodel.devolucion.ActaEntregaUiState
import com.Jair.practica.viewmodel.devolucion.ActaEntregaViewModel
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.Executors

/** RF-10: acta basada en la entrega real y la firma almacenada. */
class ActaEntregaActivity : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val cliente = OkHttpClient()
    private val viewModel: ActaEntregaViewModel by viewModels {
        ActaEntregaViewModel.Factory(
            LocalEntregaRepository(), LocalUsuarioRepository(), LocalObjetoEncontradoRepository(),
            LocalPersonaAtendidaRepository(), LocalAuditoriaRepository()
        )
    }
    private lateinit var contenido: LinearLayout
    private lateinit var firmaVista: ImageView
    private lateinit var firmaEstado: TextView
    private lateinit var descargar: Button
    private var actaActual: ActaEntrega? = null
    private var firmaActual: Bitmap? = null
    private val crearPdf = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) guardarPdf(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val (scroll, root) = AppUi.screen(this)
        root.addView(AppUi.header(this, "Acta de entrega", "Custodia SAE") { finish() })
        contenido = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(contenido)
        setContentView(scroll)
        viewModel.state.observe(this) { mostrar(it) }
        viewModel.cargar(intent.getStringExtra(EXTRA_ID_ENTREGA).orEmpty(),
            LoginRole.fromCode(intent.getStringExtra("rol")))
    }

    private fun mostrar(estado: ActaEntregaUiState) {
        contenido.removeAllViews()
        if (estado.cargando) {
            contenido.addView(mensaje("Cargando acta…"))
            return
        }
        val acta = estado.data
        if (acta == null) {
            contenido.addView(mensaje(estado.errorCarga ?: estado.errorGeneral ?: "No se encontró el acta."))
            return
        }
        actaActual = acta
        val entrega = acta.entrega
        val datos = AppUi.card(this)
        datos.addView(titulo("ACTA OFICIAL DE DEVOLUCIÓN"))
        datos.addView(linea("Número", ActaEntregaViewModel.correlativoActa(entrega)))
        datos.addView(linea("Fecha de entrega", ActaEntregaViewModel.formatearFechaCierre(entrega.fechaConfirmacion)))
        datos.addView(linea("Estado", entrega.estado.name))
        datos.addView(linea("Código de custodia", acta.objeto?.codigoCustodia ?: "Sin código"))
        datos.addView(linea("Objeto", acta.objeto?.descripcion ?: "Sin descripción"))
        datos.addView(linea("Lugar del hallazgo", acta.objeto?.lugarHallazgo ?: "No indicado"))
        datos.addView(linea("Receptor", acta.persona?.nombre ?: "No identificado"))
        datos.addView(linea("DNI", entrega.receptorDni ?: acta.persona?.dni ?: "No indicado"))
        datos.addView(linea("Correo", entrega.receptorCorreo ?: acta.persona?.correo ?: "No indicado"))
        datos.addView(linea("Administrador responsable", administrador(acta)))
        datos.addView(linea("Operación", entrega.idOperacion ?: entrega.id))
        contenido.addView(datos)

        val firma = AppUi.card(this)
        firma.addView(titulo("Firma capturada del receptor"))
        firmaVista = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(android.graphics.Color.WHITE)
            contentDescription = "Firma manuscrita del receptor"
        }
        firma.addView(firmaVista, LinearLayout.LayoutParams(-1, AppUi.dp(this, 130)))
        firmaEstado = mensaje("Cargando firma desde Supabase…")
        firma.addView(firmaEstado)
        firma.addView(AppUi.secondary(this, "Reintentar carga de firma").apply {
            setOnClickListener { cargarFirma(entrega.firmaReferencia) }
        })
        contenido.addView(firma)

        descargar = AppUi.primary(this, "Descargar acta (PDF)").apply {
            isEnabled = false
            setOnClickListener {
                if (firmaActual != null) crearPdf.launch("${ActaEntregaViewModel.correlativoActa(entrega)}.pdf")
            }
        }
        contenido.addView(descargar, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = AppUi.dp(this@ActaEntregaActivity, 14)
        })
        contenido.addView(AppUi.secondary(this, "Volver al historial").apply { setOnClickListener { finish() } })
        cargarFirma(entrega.firmaReferencia)
    }

    private fun cargarFirma(referencia: String?) {
        firmaActual = null
        descargar.isEnabled = false
        firmaVista.setImageDrawable(null)
        if (referencia.isNullOrBlank()) {
            firmaEstado.text = "Esta entrega no tiene una imagen de firma guardada."
            return
        }
        val token = SesionRemota.accessToken
        if (token.isNullOrBlank()) {
            firmaEstado.text = "La sesión caducó. Ingresa de nuevo para ver la firma."
            return
        }
        firmaEstado.text = "Cargando firma desde Supabase…"
        executor.execute {
            val resultado = runCatching {
                val solicitud = Request.Builder()
                    .url("${SupabaseConfig.AUTH_URL.substringBefore("/auth/v1/")}/storage/v1/object/evidencias-utp-objects/$referencia")
                    .header("apikey", SupabaseConfig.ANON_KEY)
                    .header("Authorization", "Bearer $token")
                    .build()
                cliente.newCall(solicitud).execute().use { respuesta ->
                    check(respuesta.isSuccessful) { "HTTP ${respuesta.code}" }
                    requireNotNull(BitmapFactory.decodeStream(respuesta.body?.byteStream())) {
                        "El archivo de firma no es una imagen válida"
                    }
                }
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                resultado.fold(
                    onSuccess = { imagen ->
                        firmaActual = imagen
                        firmaVista.setImageBitmap(imagen)
                        firmaEstado.text = "Firma del receptor cargada"
                        descargar.isEnabled = true
                    },
                    onFailure = { error -> firmaEstado.text =
                        "No se pudo cargar la firma: ${error.message ?: "error de conexión"}" }
                )
            }
        }
    }

    private fun guardarPdf(uri: Uri) {
        val acta = actaActual ?: return
        val firma = firmaActual ?: return
        val pdf = PdfDocument()
        try {
            val pagina = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            val canvas = pagina.canvas
            val titulo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AppUi.RED; textSize = 20f; isFakeBoldText = true
            }
            val texto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AppUi.INK; textSize = 12f
            }
            canvas.drawText("UTP+objects · ACTA DE ENTREGA", 40f, 55f, titulo)
            var y = 90f
            fun lineaPdf(etiqueta: String, valor: String) {
                val palabras = "$etiqueta: $valor".replace('\n', ' ').split(' ')
                var renglon = ""
                palabras.forEach { palabra ->
                    val siguiente = if (renglon.isEmpty()) palabra else "$renglon $palabra"
                    if (renglon.isNotEmpty() && texto.measureText(siguiente) > 510f) {
                        canvas.drawText(renglon, 40f, y, texto)
                        y += 18f
                        renglon = palabra
                    } else renglon = siguiente
                }
                if (renglon.isNotEmpty()) canvas.drawText(renglon, 40f, y, texto)
                y += 28f
            }
            lineaPdf("Acta", ActaEntregaViewModel.correlativoActa(acta.entrega))
            lineaPdf("Fecha", ActaEntregaViewModel.formatearFechaCierre(acta.entrega.fechaConfirmacion))
            lineaPdf("Estado", acta.entrega.estado.name)
            lineaPdf("Código de custodia", acta.objeto?.codigoCustodia.orEmpty())
            lineaPdf("Objeto", acta.objeto?.descripcion.orEmpty())
            lineaPdf("Lugar del hallazgo", acta.objeto?.lugarHallazgo.orEmpty())
            lineaPdf("Receptor", acta.persona?.nombre.orEmpty())
            lineaPdf("DNI", acta.entrega.receptorDni ?: acta.persona?.dni.orEmpty())
            lineaPdf("Correo", acta.entrega.receptorCorreo ?: acta.persona?.correo.orEmpty())
            lineaPdf("Administrador SAE", administrador(acta))
            lineaPdf("Operación", acta.entrega.idOperacion.orEmpty())
            y += 12f
            canvas.drawText("Firma capturada del receptor:", 40f, y, texto)
            y += 15f
            canvas.drawBitmap(firma, null, android.graphics.RectF(40f, y, 290f, y + 100f), null)
            pdf.finishPage(pagina)
            contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) }
                ?: error("No se pudo abrir el archivo elegido")
            Toast.makeText(this, "Acta PDF guardada correctamente.", Toast.LENGTH_LONG).show()
        } catch (error: Exception) {
            Toast.makeText(this, "No se pudo guardar el PDF: ${error.message}", Toast.LENGTH_LONG).show()
        } finally { pdf.close() }
    }

    private fun administrador(acta: ActaEntrega): String =
        acta.administrador?.nombre?.takeIf { it.isNotBlank() }
            ?: "ID ${acta.entrega.administradorId}"

    private fun titulo(valor: String) = TextView(this).apply {
        text = valor; textSize = 18f; typeface = android.graphics.Typeface.DEFAULT_BOLD
        setTextColor(AppUi.INK)
    }

    private fun linea(etiqueta: String, valor: String) = TextView(this).apply {
        text = "$etiqueta: $valor"; textSize = 14f; setTextColor(AppUi.INK)
        setPadding(0, AppUi.dp(this@ActaEntregaActivity, 8), 0, 0)
    }

    private fun mensaje(valor: String) = TextView(this).apply {
        text = valor; textSize = 13f; setTextColor(AppUi.MUTED)
        setPadding(0, AppUi.dp(this@ActaEntregaActivity, 8), 0, AppUi.dp(this@ActaEntregaActivity, 8))
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    companion object { const val EXTRA_ID_ENTREGA = "entregaId" }
}
