package com.Jair.practica.view.hallazgo

import android.os.Bundle
import android.widget.Button
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

        // Limpiamos el texto inicial porque el código se genera AL confirmar, no antes (Regla RF-03)
        tvCodigoRecepcion.text = "Código: Pendiente de generación..."

        // Recuperamos el ID real del objeto que viene del RegistroHallazgoActivity
        val idObjeto = intent.getStringExtra("ID_OBJETO_GUARDADO") ?: ""

        // Simulamos que quien abrió esta pantalla es el Administrador SAE para poder probar el flujo
        val rolActivo = LoginRole.ADMINISTRADOR_SAE

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
            Toast.makeText(this, "El objeto ahora está en el inventario.", Toast.LENGTH_SHORT).show()
        }

        btnConfirmarRecepcion.setOnClickListener {
            // Control obligatorio: Evitar doble envío
            btnConfirmarRecepcion.isEnabled = false

            // Enviamos ubicación "U-01" estática porque la vista actual no tiene selector
            viewModel.confirmarCustodia(
                rolActivo = rolActivo,
                idObjeto = idObjeto,
                idUbicacion = "U-01"
            )
        }

        btnVolverRecepcion.setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_CODIGO = "codigo_hallazgo"
    }
}