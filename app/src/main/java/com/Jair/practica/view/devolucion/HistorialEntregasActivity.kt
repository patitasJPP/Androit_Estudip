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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Jair.practica.R
import com.Jair.practica.databinding.ActivityHistorialEntregasBinding
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.LocalEntregaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.LocalPersonaAtendidaRepository
import com.Jair.practica.model.local.LocalReclamacionRepository
import com.Jair.practica.viewmodel.devolucion.HistorialEntregasViewModel

class HistorialEntregasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialEntregasBinding
    private val viewModel: HistorialEntregasViewModel by viewModels {
        HistorialEntregasViewModel.Factory(
            entregaRepository = LocalEntregaRepository(),
            objetoRepository = LocalObjetoEncontradoRepository(),
            personaRepository = LocalPersonaAtendidaRepository(),
            reclamacionRepository = LocalReclamacionRepository()
        )
    }
    private val adapter = HistorialEntregasAdapter { entrega ->
        startActivity(
            Intent(this, ActaEntregaActivity::class.java).apply {
                putExtra(ActaEntregaActivity.EXTRA_ID_ENTREGA, entrega.id)
                putExtra("usuarioId", LocalDataStore.USUARIO_ADMINISTRADOR_ID)
                putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
            }
        )
    }
    private val controller = HistorialEntregasController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialEntregasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controller.iniciar()

        binding.rvHistorialEntregas.layoutManager = LinearLayoutManager(this)
        binding.rvHistorialEntregas.adapter = adapter

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(before: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onTextoChange(s?.toString() ?: "")
            }
        })

        binding.etFiltroFechaInicio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(before: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onFechaInicioChange(s?.toString() ?: "")
            }
        })

        binding.etFiltroFechaFin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(before: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onFechaFinChange(s?.toString() ?: "")
            }
        })

        binding.etFiltroReceptor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(before: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.onFiltroReceptorChange(s?.toString() ?: "")
            }
        })

        binding.grupoFiltrosHistorial.setOnCheckedStateChangeListener { _, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            when (id) {
                R.id.chipTodas -> viewModel.limpiarFiltros()
                R.id.chipEstaSemana -> viewModel.onChipEstaSemana()
                R.id.chipLaptops -> viewModel.onCategoriaChange("Laptop")
                else -> Unit
            }
        }

        viewModel.cargar(LoginRole.fromCode(intent?.getStringExtra("rol")))

        viewModel.state.observe(this) { state ->
            val mensajeError = state.errorGeneral ?: state.errorCarga
            binding.tvErrorCarga.visibility = if (mensajeError == null) View.GONE else View.VISIBLE
            binding.tvErrorCarga.text = mensajeError
            binding.tvErrorFiltroFecha.visibility = if (state.errorFecha) View.VISIBLE else View.GONE

            binding.rvHistorialEntregas.visibility = if (state.items.isEmpty()) View.GONE else View.VISIBLE
            binding.tvHistorialVacio.visibility = if (state.listaVacia) View.VISIBLE else View.GONE
            adapter.submitList(state.items)

            if (binding.etBuscar.text.toString() != state.texto) {
                binding.etBuscar.setText(state.texto)
            }
            if (binding.etFiltroFechaInicio.text.toString() != state.fechaInicio) {
                binding.etFiltroFechaInicio.setText(state.fechaInicio)
            }
            if (binding.etFiltroFechaFin.text.toString() != state.fechaFin) {
                binding.etFiltroFechaFin.setText(state.fechaFin)
            }
            if (binding.etFiltroReceptor.text.toString() != state.texto) {
                binding.etFiltroReceptor.setText(state.texto)
            }
        }
    }
}
