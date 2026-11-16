package com.Jair.practica.view.custodia

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.PersistenciaLocal
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.model.remote.SesionRemota
import com.Jair.practica.model.remote.SincronizacionRemota
import com.Jair.practica.model.remote.SupabaseClient
import com.Jair.practica.model.remote.UbicacionCustodiaRemota
import com.Jair.practica.view.common.AppUi
import java.util.concurrent.Executors

/** RF-04: consulta completa y cambio de ubicación confirmado por Supabase. */
class GestionInventarioActivity : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val estados = listOf("Todos", "Bajo custodia", "Pendiente de recepción", "Entregado")
    private var ubicaciones = emptyList<UbicacionCustodiaRemota>()
    private var consulta = ""
    private var estadoSeleccionado = "Todos"
    private lateinit var resultados: LinearLayout
    private lateinit var mensaje: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SesionRemota.rol != "ADMINISTRADOR_SAE") {
            Toast.makeText(this, "Solo el Administrador SAE puede gestionar el inventario.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val (scroll, root) = AppUi.screen(this)
        root.addView(AppUi.header(this, "Inventario completo", "Custodia SAE") { finish() })
        val filtros = AppUi.card(this)
        filtros.addView(AppUi.sectionTitle(this, "Buscar por código, descripción, categoría o lugar"))
        filtros.addView(AppUi.field(this, "Buscar objeto").apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    consulta = s?.toString().orEmpty().trim()
                    mostrarObjetos()
                }
                override fun afterTextChanged(s: Editable?) = Unit
            })
        })
        filtros.addView(AppUi.sectionTitle(this, "Estado"))
        val selector = Spinner(this)
        selector.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        selector.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                estadoSeleccionado = estados[position]
                mostrarObjetos()
            }
        }
        filtros.addView(selector)
        root.addView(filtros)
        mensaje = TextView(this).apply { setTextColor(AppUi.MUTED); text = "Actualizando inventario…" }
        root.addView(mensaje)
        resultados = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(resultados)
        root.addView(AppUi.secondary(this, "Actualizar inventario").apply {
            setOnClickListener { actualizar() }
        })
        setContentView(scroll)
        actualizar()
    }

    private fun actualizar() {
        mensaje.text = "Actualizando inventario…"
        executor.execute {
            val respuesta = runCatching { SupabaseClient.api.consultarUbicaciones().execute() }
            main.post {
                if (isFinishing || isDestroyed) return@post
                if (respuesta.isSuccess && respuesta.getOrNull()?.isSuccessful == true) {
                    ubicaciones = respuesta.getOrNull()?.body().orEmpty().filter { it.disponible }
                } else {
                    mensaje.text = "No se pudieron cargar las ubicaciones. Comprueba la conexión."
                }
                SincronizacionRemota.refrescarObjetos {
                    if (!isFinishing && !isDestroyed) mostrarObjetos()
                }
            }
        }
    }

    private fun mostrarObjetos() {
        if (!::resultados.isInitialized || !::mensaje.isInitialized) return
        resultados.removeAllViews()
        val objetos = AlmacenLocal.instancia.objetos.filter { it.registradoPor.length == 36 }
            .filter { objeto ->
                when (estadoSeleccionado) {
                    "Bajo custodia" -> objeto.estado == EstadoObjeto.BAJO_CUSTODIA
                    "Pendiente de recepción" -> objeto.estado == EstadoObjeto.HALLAZGO_REPORTADO || objeto.estado == EstadoObjeto.PENDIENTE_RECEPCION
                    "Entregado" -> objeto.estado == EstadoObjeto.ENTREGADO
                    else -> true
                }
            }.filter { objeto ->
                consulta.isBlank() || listOf(objeto.codigoCustodia, objeto.descripcion, objeto.categoria,
                    objeto.lugarHallazgo.orEmpty(), codigoUbicacion(objeto))
                    .any { it.contains(consulta, ignoreCase = true) }
            }.sortedByDescending { it.fechaHallazgo.orEmpty() }
        mensaje.text = "${objetos.size} objeto(s) encontrados"
        objetos.forEach { objeto ->
            val tarjeta = AppUi.card(this)
            tarjeta.addView(TextView(this).apply {
                text = objeto.descripcion
                textSize = 16f
                setTextColor(AppUi.INK)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            tarjeta.addView(TextView(this).apply {
                text = "${objeto.codigoCustodia.ifBlank { "Sin código" }} · ${objeto.estado.name.replace('_', ' ')}\n" +
                    "Hallazgo: ${objeto.lugarHallazgo ?: "No indicado"}\nUbicación física: ${codigoUbicacion(objeto)}"
                setTextColor(AppUi.MUTED)
            })
            if (objeto.estado == EstadoObjeto.BAJO_CUSTODIA) {
                tarjeta.addView(AppUi.secondary(this, "Cambiar ubicación física").apply {
                    isEnabled = ubicaciones.isNotEmpty()
                    setOnClickListener { elegirUbicacion(objeto) }
                })
            }
            resultados.addView(tarjeta)
        }
    }

    private fun codigoUbicacion(objeto: ObjetoEncontrado): String =
        ubicaciones.firstOrNull { it.id == objeto.ubicacionId }?.codigo
            ?: AlmacenLocal.instancia.ubicaciones.firstOrNull { it.id == objeto.ubicacionId }?.codigo
            ?: "Sin ubicación asignada"

    private fun elegirUbicacion(objeto: ObjetoEncontrado) {
        val opciones = ubicaciones.map { "${it.codigo} · ${it.descripcion.orEmpty()}" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Ubicación de ${objeto.codigoCustodia}")
            .setItems(opciones) { _, posicion ->
                val elegida = ubicaciones[posicion]
                if (elegida.id == objeto.ubicacionId) return@setItems
                AlertDialog.Builder(this)
                    .setMessage("¿Guardar ${elegida.codigo} como nueva ubicación de ${objeto.descripcion}?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Guardar") { _, _ -> guardarUbicacion(objeto, elegida) }
                    .show()
            }.show()
    }

    private fun guardarUbicacion(objeto: ObjetoEncontrado, ubicacion: UbicacionCustodiaRemota) {
        mensaje.text = "Guardando ubicación en Supabase…"
        executor.execute {
            val resultado = runCatching {
                val idRemoto = if (runCatching { java.util.UUID.fromString(objeto.id) }.isSuccess) objeto.id
                    else {
                        val encontrado = SupabaseClient.api.buscarObjetoPorCodigo(
                            "eq.${objeto.codigoCustodia}"
                        ).execute()
                        check(encontrado.isSuccessful) { "No se encontró el objeto en Supabase (HTTP ${encontrado.code()})" }
                        requireNotNull(encontrado.body()?.singleOrNull()?.id) {
                            "El código de custodia no identifica un objeto único en Supabase"
                        }
                    }
                SupabaseClient.api.actualizarUbicacion(
                    id = "eq.$idRemoto",
                    cambio = mapOf("ubicacion_id" to ubicacion.id)
                ).execute()
            }
            main.post {
                if (isFinishing || isDestroyed) return@post
                val respuesta = resultado.getOrNull()
                if (respuesta?.isSuccessful == true && !respuesta.body().isNullOrEmpty()) {
                    val store = AlmacenLocal.instancia
                    val indice = store.objetos.indexOfFirst { it.id == objeto.id }
                    if (indice >= 0) store.objetos[indice] = store.objetos[indice].copy(ubicacionId = ubicacion.id)
                    PersistenciaLocal.guardarObjetos(store.objetos)
                    mostrarObjetos()
                    Toast.makeText(this, "Ubicación actualizada: ${ubicacion.codigo}", Toast.LENGTH_LONG).show()
                } else {
                    mensaje.text = if (resultado.isFailure)
                        "No se pudo cambiar la ubicación: ${resultado.exceptionOrNull()?.message ?: "error de conexión"}"
                    else "No se pudo cambiar la ubicación en Supabase (HTTP ${respuesta?.code() ?: "sin respuesta"})."
                }
            }
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
