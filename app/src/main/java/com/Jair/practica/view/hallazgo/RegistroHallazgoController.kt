package com.Jair.practica.view.hallazgo

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.Jair.practica.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroHallazgoController(
    private val activity: AppCompatActivity,
    private val abrirCamara: () -> Unit
) {

    // ============ DATOS INTERNOS (sin recursos externos) ============
    private val codigoAyudador = "1010"
    private val colorRojo = Color.parseColor("#B71C1C")
    private val colorPisoDefault = Color.parseColor("#F1F4F9")
    private val colorAreaDefault = Color.parseColor("#F1F4F9")
    private val colorTorreDefault = Color.parseColor("#FFFFFF")
    private val colorSalonDefault = Color.parseColor("#FFFFFF")

    private val pisos = listOf(
        "Piso 1", "Piso 2", "Piso 3", "Piso 4",
        "Piso 5", "Piso 6", "Piso 7", "Piso 8"
    )
    private val pisoChips = listOf(
        ChipRef(R.id.chipPiso1, R.id.tvPiso1),
        ChipRef(R.id.chipPiso2, R.id.tvPiso2),
        ChipRef(R.id.chipPiso3, R.id.tvPiso3),
        ChipRef(R.id.chipPiso4, R.id.tvPiso4),
        ChipRef(R.id.chipPiso5, R.id.tvPiso5),
        ChipRef(R.id.chipPiso6, R.id.tvPiso6),
        ChipRef(R.id.chipPiso7, R.id.tvPiso7),
        ChipRef(R.id.chipPiso8, R.id.tvPiso8)
    )

    private val torres = listOf("Torre A", "Torre B", "Torre C", "Torre D")
    private val torreChips = listOf(
        ChipRef(R.id.chipTorreA, R.id.tvTorreA),
        ChipRef(R.id.chipTorreB, R.id.tvTorreB),
        ChipRef(R.id.chipTorreC, R.id.tvTorreC),
        ChipRef(R.id.chipTorreD, R.id.tvTorreD)
    )

    private val areas = listOf("Salón", "Pasillo", "Baños", "Escaleras")
    private val areaChips = listOf(
        ChipRef(R.id.chipAreaSalon, R.id.tvAreaSalon, R.id.imgAreaSalon),
        ChipRef(R.id.chipAreaPasillo, R.id.tvAreaPasillo, R.id.imgAreaPasillo),
        ChipRef(R.id.chipAreaBanos, R.id.tvAreaBanos, R.id.imgAreaBanos),
        ChipRef(R.id.chipAreaEscaleras, R.id.tvAreaEscaleras, R.id.imgAreaEscaleras)
    )

    private val salones = listOf(
        Salon("A0601", "Teoría A"),
        Salon("A0602", "Lab. Redes"),
        Salon("A0603", "Teoría B"),
        Salon("A0604", "Taller CAD"),
        Salon("A0605", "Aula Magna"),
        Salon("A0606", "Cómputo 02")
    )
    private val salonChips = listOf(
        SalonRef(R.id.itemSalonA0601, R.id.tvCodigoSalonA0601, R.id.tvNombreSalonA0601),
        SalonRef(R.id.itemSalonA0602, R.id.tvCodigoSalonA0602, R.id.tvNombreSalonA0602),
        SalonRef(R.id.itemSalonA0603, R.id.tvCodigoSalonA0603, R.id.tvNombreSalonA0603),
        SalonRef(R.id.itemSalonA0604, R.id.tvCodigoSalonA0604, R.id.tvNombreSalonA0604),
        SalonRef(R.id.itemSalonA0605, R.id.tvCodigoSalonA0605, R.id.tvNombreSalonA0605),
        SalonRef(R.id.itemSalonA0606, R.id.tvCodigoSalonA0606, R.id.tvNombreSalonA0606)
    )

    // ============ ESTADO DE SELECCIÓN ============
    private var torreSeleccionada = torres[0]
    private var pisoSeleccionado = pisos[5]
    private var areaSeleccionada = areas[0]
    private var salonSeleccionado = salones[0]
    private var fechaSello = ""
    private var horaSello = ""
    private var fotoTomada = false

    fun iniciar() {
        fijarSelloTemporal()
        configurarVolver()
        configurarCamara()
        configurarPisos()
        configurarAreas()
        configurarTorres()
        configurarSalones()
        configurarBuscador()
        configurarContadorDescripcion()
        configurarRegistro()

        seleccionarPiso(5)
        seleccionarArea(0)
        seleccionarTorre(0)
        seleccionarSalon(0)
        actualizarSalonesDisponibles()
    }

    // ============ SELLO TEMPORAL ============
    private fun fijarSelloTemporal() {
        val ahora = Date()
        val locale = Locale.forLanguageTag("es-PE")
        val formatoFecha = SimpleDateFormat("EEE, dd MMM. yyyy", locale).format(ahora)
        val formatoHora = SimpleDateFormat("hh:mm:ss a", locale).format(ahora)
        fechaSello = formatoFecha.uppercase()
        horaSello = formatoHora
        activity.findViewById<TextView>(R.id.tvFechaHallazgo).text = fechaSello
        activity.findViewById<TextView>(R.id.tvHoraHallazgo).text = horaSello
    }

    // ============ VOLVER ============
    private fun configurarVolver() {
        val accion = View.OnClickListener { activity.finish() }
        activity.findViewById<View>(R.id.imgVolver).setOnClickListener(accion)
        activity.findViewById<View>(R.id.tvVolver).setOnClickListener(accion)
    }

    // ============ CÁMARA ============
    private fun configurarCamara() {
        val abrir = View.OnClickListener { abrirCamara() }
        activity.findViewById<View>(R.id.btnAbrirCamara).setOnClickListener(abrir)
        activity.findViewById<View>(R.id.cardZonaFoto).setOnClickListener(abrir)
        activity.findViewById<View>(R.id.cardCirculoCamara).setOnClickListener(abrir)
        activity.findViewById<View>(R.id.imgCamaraCirculo).setOnClickListener(abrir)
        activity.findViewById<View>(R.id.tvTomarFoto).setOnClickListener(abrir)
    }

    fun mostrarFoto(bitmap: Bitmap?) {
        if (bitmap == null) return
        activity.findViewById<ImageView>(R.id.imgFotoPreview).apply {
            setImageBitmap(bitmap)
            visibility = View.VISIBLE
        }
        listOf(R.id.cardCirculoCamara, R.id.tvTomarFoto, R.id.tvCapturaNitida).forEach {
            activity.findViewById<View>(it).visibility = View.GONE
        }
        fotoTomada = true
    }

    // ============ PISOS ============
    private fun configurarPisos() {
        pisoChips.forEachIndexed { indice, chip ->
            activity.findViewById<View>(chip.cardId).setOnClickListener {
                seleccionarPiso(indice)
            }
        }
    }

    private fun seleccionarPiso(seleccionado: Int) {
        pisoChips.forEachIndexed { indice, chip ->
            val marcado = indice == seleccionado
            val card = activity.findViewById<MaterialCardView>(chip.cardId)
            val texto = activity.findViewById<TextView>(chip.textId)
            card.setCardBackgroundColor(if (marcado) colorRojo else colorPisoDefault)
            texto.setTextColor(if (marcado) Color.WHITE else Color.parseColor("#374151"))
            texto.setTypeface(null, if (marcado) Typeface.BOLD else Typeface.NORMAL)
        }
        pisoSeleccionado = pisos[seleccionado]
        activity.findViewById<TextView>(R.id.tvPisoSeleccionado).text = pisoSeleccionado
        actualizarSalonesDisponibles()
    }

    // ============ ÁREAS (SALÓN / PASILLO / BAÑOS / ESCALERAS) ============
    private fun configurarAreas() {
        areaChips.forEachIndexed { indice, chip ->
            activity.findViewById<View>(chip.cardId).setOnClickListener {
                seleccionarArea(indice)
            }
        }
    }

    private fun seleccionarArea(seleccionado: Int) {
        areaChips.forEachIndexed { indice, chip ->
            val marcado = indice == seleccionado
            val card = activity.findViewById<MaterialCardView>(chip.cardId)
            val icono = activity.findViewById<ImageView>(chip.imgId)
            val texto = activity.findViewById<TextView>(chip.textId)
            card.setCardBackgroundColor(if (marcado) colorRojo else colorAreaDefault)
            icono.imageTintList = ColorStateList.valueOf(
                if (marcado) Color.WHITE else Color.parseColor("#6B7280")
            )
            texto.setTextColor(if (marcado) Color.WHITE else Color.parseColor("#4B5563"))
            texto.setTypeface(null, if (marcado) Typeface.BOLD else Typeface.NORMAL)
        }
        areaSeleccionada = areas[seleccionado]
    }

    // ============ TORRES ============
    private fun configurarTorres() {
        torreChips.forEachIndexed { indice, chip ->
            activity.findViewById<View>(chip.cardId).setOnClickListener {
                seleccionarTorre(indice)
            }
        }
    }

    private fun seleccionarTorre(seleccionado: Int) {
        torreChips.forEachIndexed { indice, chip ->
            val marcado = indice == seleccionado
            val card = activity.findViewById<MaterialCardView>(chip.cardId)
            val texto = activity.findViewById<TextView>(chip.textId)
            card.setCardBackgroundColor(if (marcado) colorRojo else colorTorreDefault)
            card.setStrokeColor(ColorStateList.valueOf(
                if (marcado) colorRojo else Color.parseColor("#E5E7EB")
            ))
            card.strokeWidth = if (marcado) 0 else 1
            texto.setTextColor(if (marcado) Color.WHITE else Color.parseColor("#374151"))
            texto.setTypeface(null, if (marcado) Typeface.BOLD else Typeface.NORMAL)
        }
        torreSeleccionada = torres[seleccionado]
        actualizarSalonesDisponibles()
    }

    // ============ SALONES ============
    private fun configurarSalones() {
        salonChips.forEachIndexed { indice, chip ->
            activity.findViewById<View>(chip.cardId).setOnClickListener {
                seleccionarSalon(indice)
            }
        }
    }

    private fun seleccionarSalon(seleccionado: Int) {
        salonChips.forEachIndexed { indice, chip ->
            val marcado = indice == seleccionado
            val card = activity.findViewById<MaterialCardView>(chip.cardId)
            val codigo = activity.findViewById<TextView>(chip.codigoId)
            val nombre = activity.findViewById<TextView>(chip.nombreId)
            card.setCardBackgroundColor(if (marcado) colorRojo else colorSalonDefault)
            card.setStrokeColor(ColorStateList.valueOf(
                if (marcado) colorRojo else Color.parseColor("#E5E7EB")
            ))
            card.strokeWidth = 1
            codigo.setTextColor(if (marcado) Color.WHITE else Color.parseColor("#111827"))
            nombre.setTextColor(if (marcado) Color.parseColor("#FECACA") else Color.parseColor("#6B7280"))
        }
        salonSeleccionado = salones[seleccionado]
    }

    private fun actualizarSalonesDisponibles() {
        activity.findViewById<TextView>(R.id.tvSalonesDisponibles).text =
            "Salones disponibles en $torreSeleccionada - $pisoSeleccionado:"
    }

    // ============ BUSCADOR DE SALONES ============
    private fun configurarBuscador() {
        activity.findViewById<EditText>(R.id.etBuscarSalon).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val busqueda = s?.toString()?.trim().orEmpty()
                if (busqueda.isEmpty()) {
                    seleccionarSalon(salones.indexOf(salonSeleccionado).coerceAtLeast(0))
                    return
                }
                val coincidencias = salones.indices.filter {
                    salones[it].codigo.contains(busqueda, ignoreCase = true) ||
                        salones[it].nombre.contains(busqueda, ignoreCase = true)
                }
                if (coincidencias.isNotEmpty()) {
                    seleccionarSalon(coincidencias.first())
                } else {
                    salonesSeleccionNinguno()
                }
            }
        })
    }

    private fun salonesSeleccionNinguno() {
        salonChips.forEachIndexed { _, chip ->
            val card = activity.findViewById<MaterialCardView>(chip.cardId)
            val codigo = activity.findViewById<TextView>(chip.codigoId)
            val nombre = activity.findViewById<TextView>(chip.nombreId)
            card.setCardBackgroundColor(colorSalonDefault)
            card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E5E7EB")))
            codigo.setTextColor(Color.parseColor("#111827"))
            nombre.setTextColor(Color.parseColor("#6B7280"))
        }
    }

    // ============ CONTADOR DE DESCRIPCIÓN ============
    private fun configurarContadorDescripcion() {
        val campo = activity.findViewById<EditText>(R.id.etDescripcionObjeto)
        val contador = activity.findViewById<TextView>(R.id.tvContadorDescripcion)
        campo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                contador.text = "${s?.length ?: 0}/240"
            }
        })
    }

    // ============ REGISTRO ============
    private fun configurarRegistro() {
        activity.findViewById<View>(R.id.btnRegistrarEnviar).setOnClickListener {
            registrarHallazgo()
        }
    }

    private fun registrarHallazgo() {
        val descripcion = activity.findViewById<EditText>(R.id.etDescripcionObjeto)
            .text.toString().trim().ifEmpty { "Sin descripción" }
        val codigoUnico = activity.findViewById<TextView>(R.id.tvCodigoUnico).text.toString()
        val anaquel = activity.findViewById<TextView>(R.id.tvAnaquelSeleccionado).text.toString()
        val campus = activity.findViewById<TextView>(R.id.tvSedeCampus).text.toString()
        val campusCentral = activity.findViewById<TextView>(R.id.tvCampusCentral).text.toString()

        val resumen = buildString {
            appendLine("=== REGISTRO DE HALLAZGO ===")
            appendLine("Ayudador: $codigoAyudador")
            appendLine("Código único: $codigoUnico")
            appendLine("Descripción: $descripcion")
            appendLine("--- UBICACIÓN ---")
            appendLine("Campus: $campusCentral")
            appendLine("Sede: $campus")
            appendLine("Torre: $torreSeleccionada")
            appendLine("Piso: $pisoSeleccionado")
            appendLine("Área específica: $areaSeleccionada")
            appendLine("Salón: ${salonSeleccionado.codigo}")
            appendLine("Nombre del salón: ${salonSeleccionado.nombre}")
            appendLine("Nomenclatura: $torreSeleccionada • $pisoSeleccionado • $areaSeleccionada • ${salonSeleccionado.codigo}")
            appendLine("--- CUSTODIA ---")
            appendLine("Anaquel: $anaquel")
            appendLine("Foto: ${if (fotoTomada) "Capturada" else "Pendiente"}")
            appendLine("--- SELLO TEMPORAL ---")
            appendLine("Fecha: $fechaSello")
            append("Hora: $horaSello")
        }

        Toast.makeText(activity, resumen, Toast.LENGTH_LONG).show()
    }

    private data class ChipRef(val cardId: Int, val textId: Int, val imgId: Int = 0)
    private data class Salon(val codigo: String, val nombre: String)
    private data class SalonRef(val cardId: Int, val codigoId: Int, val nombreId: Int)
}