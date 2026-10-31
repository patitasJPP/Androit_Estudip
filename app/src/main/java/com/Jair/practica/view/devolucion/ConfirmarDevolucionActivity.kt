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
import com.Jair.practica.databinding.ActivityConfirmarDevolucionBinding
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.LocalAuditoriaRepository
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.LocalEntregaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.LocalPersonaAtendidaRepository
import com.Jair.practica.model.local.LocalReclamacionRepository
import com.Jair.practica.view.common.NavegacionPrincipal
import com.Jair.practica.view.devolucion.HistorialEntregasActivity
import com.Jair.practica.viewmodel.devolucion.ConfirmarDevolucionViewModel

class ConfirmarDevolucionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfirmarDevolucionBinding
    private val viewModel: ConfirmarDevolucionViewModel by viewModels {
        ConfirmarDevolucionViewModel.Factory(
            reclamacionRepository = LocalReclamacionRepository(),
            objetoRepository = LocalObjetoEncontradoRepository(),
            personaRepository = LocalPersonaAtendidaRepository(),
            entregaRepository = LocalEntregaRepository()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmarDevolucionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clVolver.setOnClickListener { NavegacionPrincipal.panel(this) }

        binding.btnLimpiarFirma.setOnClickListener {
            binding.viewFirma.limpiar()
            viewModel.onFirmaChange(false)
        }

        binding.btnConfirmarDevolucion.setOnClickListener {
            viewModel.confirmar()
        }

        binding.viewFirma.cambioEstadoFirma = { capturada ->
            viewModel.onFirmaChange(capturada)
        }

        binding.etReceptorId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(before: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onReceptorChange(s?.toString() ?: "")
            }
        })

        binding.etDni.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(before: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onDniChange(s?.toString() ?: "")
            }
        })

        binding.etCorreo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(before: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onCorreoChange(s?.toString() ?: "")
            }
        })

        viewModel.cargar(intent?.getStringExtra(EXTRA_RECLAMACION) ?: LocalDataStore.RECLAMACION_DEMO_ID)

        viewModel.state.observe(this) { state ->
            binding.tvErrorCarga.visibility = if (state.errorCarga == null) View.GONE else View.VISIBLE
            binding.tvErrorCarga.text = state.errorCarga

            state.reclamacion?.let { reclamacion ->
                binding.tvEstadoReclamacion.text = when (reclamacion.estado) {
                    EstadoReclamacion.APROBADA -> "Aprobada"
                    EstadoReclamacion.PENDIENTE_VERIFICACION -> "Pendiente de verificación"
                    EstadoReclamacion.RECHAZADA -> "Rechazada"
                    EstadoReclamacion.ATENDIDA -> "Atendida"
                }
                binding.tvCodigoObjeto.text = state.objeto?.codigoCustodia ?: ""
                binding.tvCategoriaObjeto.text = state.objeto?.categoria ?: ""
                binding.tvDescripcionObjeto.text = state.objeto?.descripcion ?: ""
            }

            binding.etReceptorId.setText(state.receptorId)
            binding.etDni.setText(state.dni)
            binding.etCorreo.setText(state.correo)

            binding.tvErrorReceptor.visibility = if (state.errorReceptor) View.VISIBLE else View.GONE
            binding.tvErrorDni.visibility = if (state.errorDni) View.VISIBLE else View.GONE
            binding.tvErrorCorreo.visibility = if (state.errorCorreo) View.VISIBLE else View.GONE
            binding.tvErrorFirma.visibility = if (state.errorFirma) View.VISIBLE else View.GONE
            binding.tvErrorGeneral.visibility = if (state.errorGeneral != null) View.VISIBLE else View.GONE
            state.errorGeneral?.let { binding.tvErrorGeneral.text = it }
            binding.tvMensajeExito.visibility = if (state.mensajeExito != null) View.VISIBLE else View.GONE
            state.mensajeExito?.let { binding.tvMensajeExito.text = it }

            binding.btnConfirmarDevolucion.isEnabled =
                !state.cargando && !state.errorAcceso && !state.bloqueada && !state.enviando &&
                    state.decision == EstadoReclamacion.APROBADA && state.firmaCapturada

            if (state.mensajeExito != null) {
                binding.tvMensajeExito.post {
                    if (!isDestroyed) {
                        startActivity(
                            Intent(this, HistorialEntregasActivity::class.java).apply {
                                putExtra("usuarioId", LocalDataStore.USUARIO_ADMINISTRADOR_ID)
                                putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
                            }
                        )
                        finish()
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_RECLAMACION = "reclamacionId"
    }
}
