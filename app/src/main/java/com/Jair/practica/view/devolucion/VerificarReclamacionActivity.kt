package com.Jair.practica.view.devolucion

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.databinding.ActivityVerificarReclamacionBinding
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.LocalAuditoriaRepository
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.LocalEvidenciaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.LocalPersonaAtendidaRepository
import com.Jair.practica.model.local.LocalReclamacionRepository
import com.Jair.practica.model.repository.AuditoriaRepository
import com.Jair.practica.model.repository.EvidenciaRepository
import com.Jair.practica.model.repository.ObjetoEncontradoRepository
import com.Jair.practica.model.repository.PersonaAtendidaRepository
import com.Jair.practica.model.repository.ReclamacionRepository
import com.Jair.practica.viewmodel.devolucion.VerificarReclamacionViewModel
import com.Jair.practica.viewmodel.devolucion.VerificarReclamacionViewModel.Companion.etiquetaEstado

class VerificarReclamacionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerificarReclamacionBinding
    private var actualizandoPreguntas = false
    private val viewModel: VerificarReclamacionViewModel by viewModels {
        VerificarReclamacionViewModel.Factory(
            reclamacionRepository = LocalReclamacionRepository(),
            evidenciaRepository = LocalEvidenciaRepository(),
            auditoriaRepository = LocalAuditoriaRepository(),
            objetoRepository = LocalObjetoEncontradoRepository(),
            personaRepository = LocalPersonaAtendidaRepository()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificarReclamacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clVolver.setOnClickListener { finish() }

        binding.btnRegistrarDecision.setOnClickListener {
            viewModel.registrarDecision()
        }

        binding.btnAgregarPregunta.setOnClickListener {
            val texto = binding.etNuevaPregunta.text?.toString().orEmpty()
            if (viewModel.agregarPregunta(texto)) {
                binding.etNuevaPregunta.text?.clear()
            }
        }

        binding.etMotivo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(before: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onMotivoChange(s?.toString() ?: "")
            }
        })

        binding.rgDecisionReclamacion.setOnCheckedChangeListener { _, checkedId ->
            val estado = when (checkedId) {
                binding.rbDecisionAprobar.id -> EstadoReclamacion.APROBADA
                binding.rbDecisionPendiente.id -> EstadoReclamacion.PENDIENTE_VERIFICACION
                binding.rbDecisionRechazar.id -> EstadoReclamacion.RECHAZADA
                else -> null
            }
            viewModel.seleccionarDecision(estado)
        }

        viewModel.cargar(intent?.getStringExtra(EXTRA_RECLAMACION) ?: LocalDataStore.RECLAMACION_DEMO_ID)

        viewModel.state.observe(this) { state ->
            binding.progressVerificar.visibility = if (state.cargando) View.VISIBLE else View.GONE
            binding.tvErrorCarga.visibility = if (state.errorCarga == null) View.GONE else View.VISIBLE
            binding.tvErrorCarga.text = state.errorCarga

            if (state.errorAcceso) {
                binding.btnRegistrarDecision.isEnabled = false
                binding.etMotivo.isEnabled = false
                binding.etNuevaPregunta.isEnabled = false
                binding.btnAgregarPregunta.isEnabled = false
            }

            state.reclamacion?.let { reclamacion ->
                binding.tvNombreSolicitante.text = state.persona?.nombre ?: "Solicitante no identificado"
                binding.tvVinculoSolicitante.text = state.persona?.vinculoUniversidad
                binding.tvIdentificadorSolicitante.text = state.persona?.identificador
                binding.tvFechaReclamacion.text = reclamacion.fechaRegistro ?: ""
                binding.tvEstadoActual.text = etiquetaEstado(reclamacion.estado)
                binding.tvValorComparacionReclamante.text = reclamacion.descripcionReclamante ?: ""
                binding.tvValorComparacionRegistrado.text = state.objeto?.descripcion
                binding.tvValorCaracteristicasReclamante.text = reclamacion.caracteristicasDeclaradas ?: ""
                binding.tvValorCaracteristicasReservado.text = state.objeto?.caracteristicasReservadas
                binding.tvEvidenciasTexto.text = state.evidencias.joinToString("\n") { evidencia ->
                    "Evidencia ${evidencia.tipo}: ${evidencia.descripcion}" +
                        (if (evidencia.reservada) " (reservada)" else "")
                }
            }

            state.comparacion.forEachIndexed { index, campo ->
                if (index == 0) {
                    binding.tvValorComparacionReclamante.text = campo.valorReclamante
                    binding.tvValorComparacionRegistrado.text = campo.valorRegistrado
                } else {
                    binding.tvValorCaracteristicasReclamante.text = campo.valorReclamante
                    binding.tvValorCaracteristicasReservado.text = campo.valorRegistrado
                }
            }

            binding.cardCaracteristicasReservadas.visibility =
                if (state.mostrarCaracteristicasReservadas) View.VISIBLE else View.GONE
            binding.tvValorCaracteristicasReservadas.text = state.objeto?.caracteristicasReservadas ?: ""

            binding.llPreguntas.removeAllViews()
            actualizandoPreguntas = true
            state.preguntas.forEach { pregunta ->
                val row = layoutInflater.inflate(
                    R.layout.item_pregunta_verificacion,
                    binding.llPreguntas,
                    false
                )
                row.findViewById<TextView>(R.id.tvPreguntaItem).text = pregunta.texto
                val respuesta = row.findViewById<EditText>(R.id.etRespuestaItem)
                respuesta.setText(pregunta.respuesta ?: "")
                respuesta.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                    override fun afterTextChanged(s: Editable?) {
                        if (!actualizandoPreguntas) {
                            viewModel.onRespuestaChange(pregunta.id, s?.toString().orEmpty())
                        }
                    }
                })
                binding.llPreguntas.addView(row)
            }
            actualizandoPreguntas = false

            binding.tvErrorPregunta.visibility = if (state.errorPregunta) View.VISIBLE else View.GONE
            binding.tvErrorDecision.visibility = if (state.errorDecision) View.VISIBLE else View.GONE
            binding.tvErrorMotivo.visibility = if (state.errorMotivo) View.VISIBLE else View.GONE
            binding.tvMensajeExito.visibility = if (state.mensajeExito != null) View.VISIBLE else View.GONE
            state.mensajeExito?.let { binding.tvMensajeExito.text = it }
            binding.tvErrorGeneral.visibility = if (state.errorGeneral != null) View.VISIBLE else View.GONE
            state.errorGeneral?.let { binding.tvErrorGeneral.text = it }

            if (state.mensajeExito != null && state.reclamacion?.estado == EstadoReclamacion.APROBADA) {
                binding.tvMensajeExito.post {
                    if (!isDestroyed) {
                        startActivity(
                            Intent(
                                this,
                                ConfirmarDevolucionActivity::class.java
                            ).apply {
                                putExtra(ConfirmarDevolucionActivity.EXTRA_RECLAMACION, state.reclamacion?.id)
                                putExtra("usuarioId", LocalDataStore.USUARIO_ADMINISTRADOR_ID)
                                putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
                            }
                        )
                        finish()
                    }
                }
            }

            binding.btnRegistrarDecision.isEnabled =
                !state.cargando && !state.errorAcceso && !state.bloqueada && !state.enviando && state.decisionSeleccionada

            binding.etMotivo.isEnabled = !state.errorAcceso
            binding.etNuevaPregunta.isEnabled = !state.errorAcceso
            binding.btnAgregarPregunta.isEnabled = !state.errorAcceso
        }
    }

    companion object {
        const val EXTRA_RECLAMACION = "reclamacionId"
    }
}
