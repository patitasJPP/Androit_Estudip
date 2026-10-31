package com.Jair.practica.view.hallazgo

import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.viewmodel.hallazgo.ConfirmarRecepcionViewModel

/** RF-03: confirmación de recepción física y ubicación bajo custodia. */
class ConfirmarRecepcionActivity : AppCompatActivity() {

    private val viewModel: ConfirmarRecepcionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmar_recepcion)

        val tvCodigoRecepcion = findViewById<TextView>(R.id.tvCodigoRecepcion)
        val tvEstadoRecepcion = findViewById<TextView>(R.id.tvEstadoRecepcion)
        val btnConfirmarRecepcion = findViewById<Button>(R.id.btnConfirmarRecepcion)
        val btnVolverRecepcion = findViewById<Button>(R.id.btnVolverRecepcion)
        val spinner = findViewById<Spinner>(R.id.spinnerUbicacionCustodia)

        // Limpiamos el texto inicial porque el código se genera AL confirmar, no antes (Regla RF-03)
        tvCodigoRecepcion.text = "Código: Pendiente de generación..."

        // Recuperamos el ID real del objeto que viene del RegistroHallazgoActivity
        val idObjeto = intent.getStringExtra("ID_OBJETO_GUARDADO") ?: ""

        // Simulamos que quien abrió esta pantalla es el Administrador SAE para poder probar el flujo
        val rolActivo = LoginRole.fromCode(
            intent.getStringExtra("rol") ?: LoginRole.PERSONAL_REGISTRADOR.code
        )
        viewModel.cargarUbicaciones()
        viewModel.ubicaciones.observe(this) { ubicaciones ->
            spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                ubicaciones.map { "${it.codigo} · ${it.descripcion.orEmpty()}" })
        }

        // Observamos errores de negocio (Ej. si entrara un Personal Registrador)
        viewModel.mensajeError.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            btnConfirmarRecepcion.isEnabled = true
        }

        // Observamos el éxito de la operación en el repositorio local
        viewModel.confirmacionExitosa.observe(this) { objeto ->
            // Mostramos el código de custodia real generado por el ViewModel
            tvCodigoRecepcion.text = "CÓDIGO GENERADO: ${objeto.codigoCustodia}"
            tvEstadoRecepcion.text = "Recepción confirmada · Objeto bajo custodia"
            com.Jair.practica.model.local.AlmacenLocal.instancia.ultimoObjetoPendienteId = null
            Toast.makeText(this, "El objeto ahora está en el inventario.", Toast.LENGTH_SHORT).show()
        }

        btnConfirmarRecepcion.setOnClickListener {
            // Control obligatorio: Evitar doble envío
            btnConfirmarRecepcion.isEnabled = false

            val ubicacionId = viewModel.ubicaciones.value?.getOrNull(spinner.selectedItemPosition)?.id.orEmpty()
            viewModel.confirmarCustodia(
                rolActivo = rolActivo,
                idObjeto = idObjeto,
                idUbicacion = ubicacionId
            )
        }

        btnVolverRecepcion.setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_CODIGO = "codigo_hallazgo"
    }
}
