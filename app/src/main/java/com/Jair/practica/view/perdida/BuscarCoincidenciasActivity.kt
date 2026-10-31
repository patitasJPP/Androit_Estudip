package com.Jair.practica.view.perdida

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.Jair.practica.R
import com.Jair.practica.databinding.ActivityBuscarCoincidenciasBinding
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.AlmacenPerdidas
import com.Jair.practica.model.local.LocalReportePerdidaRepository
import com.Jair.practica.model.perdida.CatalogoPerdidas
import com.Jair.practica.model.perdida.FiltrosCoincidencia
import com.Jair.practica.model.perdida.etiqueta
import com.Jair.practica.viewmodel.perdida.BuscarCoincidenciasUiState
import com.Jair.practica.viewmodel.perdida.BuscarCoincidenciasViewModel

class BuscarCoincidenciasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuscarCoincidenciasBinding

    private val actorId: String by lazy {
        intent.getStringExtra(RegistrarPerdidaActivity.EXTRA_USUARIO_ID)
            ?: AlmacenPerdidas.USUARIO_REGISTRADOR_ID
    }
    private val actorRol: LoginRole by lazy {
        LoginRole.fromCode(intent.getStringExtra(RegistrarPerdidaActivity.EXTRA_ROL))
    }
    private val idReporte: String by lazy { intent.getStringExtra(EXTRA_ID_REPORTE).orEmpty() }

    private val viewModel: BuscarCoincidenciasViewModel by viewModels {
        BuscarCoincidenciasViewModel.Factory(LocalReportePerdidaRepository(), actorId, actorRol)
    }

    private val adapter = CoincidenciasAdapter { objetoId -> viewModel.darSeguimiento(objetoId) }

    /** Los filtros se rellenan una sola vez con los datos del reporte; después manda lo que escribe el usuario. */
    private var filtrosIniciales = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuscarCoincidenciasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ajustarInsets(binding.rootBuscarCoincidencias)

        binding.actFPabellon.cargarOpciones(CatalogoPerdidas.PABELLONES)
        binding.actFPiso.cargarOpciones(CatalogoPerdidas.PISOS)
        binding.actFCategoria.cargarOpciones(CatalogoPerdidas.CATEGORIAS)
        binding.actFColor.cargarOpciones(CatalogoPerdidas.COLORES)

        binding.rvCoincidencias.layoutManager = LinearLayoutManager(this)
        binding.rvCoincidencias.adapter = adapter

        binding.ivVolverBusqueda.setOnClickListener { finish() }
        binding.btnBuscar.setOnClickListener { viewModel.buscar(leerFiltros()) }
        binding.btnLimpiarFiltros.setOnClickListener {
            mostrarFiltros(FiltrosCoincidencia())
            viewModel.limpiarFiltros()
        }

        viewModel.state.observe(this) { render(it) }
        if (savedInstanceState == null) viewModel.cargar(idReporte)
    }

    private fun render(s: BuscarCoincidenciasUiState) {
        val sinAcceso = s.accesoDenegado || s.reporteNoEncontrado
        binding.layoutBusqueda.visibility = if (sinAcceso) View.GONE else View.VISIBLE
        binding.tvAccesoDenegadoBusqueda.visibility = if (sinAcceso) View.VISIBLE else View.GONE
        binding.tvAccesoDenegadoBusqueda.setText(
            if (s.accesoDenegado) R.string.perdida_acceso_denegado else R.string.perdida_no_encontrado
        )
        if (sinAcceso) return

        val reporte = s.reporte
        if (reporte != null) {
            binding.tvResumenReporte.text = reporte.descripcion
            binding.tvEstadoCaso.text = getString(R.string.perdida_caso_estado, reporte.estado.etiqueta())
            if (!filtrosIniciales) {
                filtrosIniciales = true
                mostrarFiltros(s.filtros)
            }
        }

        binding.btnBuscar.isEnabled = !s.cargando && !s.bloqueado
        binding.tvErrorFiltros.text = s.errorFiltros
        binding.tvErrorFiltros.visibility = if (s.errorFiltros != null) View.VISIBLE else View.GONE

        val puedeSeguir = !s.bloqueado && reporte != null
        adapter.enviar(
            s.coincidencias,
            if (reporte != null && reporte.estado == EstadoReportePerdida.EN_SEGUIMIENTO) reporte.objetoSeguimientoId else null,
            puedeSeguir
        )
        binding.rvCoincidencias.visibility = if (s.coincidencias.isEmpty()) View.GONE else View.VISIBLE
        binding.tvSinResultados.visibility = if (s.coincidencias.isEmpty()) View.VISIBLE else View.GONE
        binding.tvSinResultados.setText(
            when {
                s.bloqueado -> R.string.perdida_bloqueado
                s.busquedaRealizada -> R.string.perdida_sin_resultados
                else -> R.string.perdida_sin_buscar
            }
        )

        s.mensaje?.let {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            viewModel.mensajeConsumido()
        }
    }

    private fun mostrarFiltros(f: FiltrosCoincidencia) {
        binding.actFPabellon.mostrarValor(f.pabellon)
        binding.actFPiso.mostrarValor(f.piso)
        binding.etFAula.setText(f.aula)
        binding.actFCategoria.mostrarValor(f.categoria)
        binding.actFColor.mostrarValor(f.color)
        binding.etFMarca.setText(f.marca)
        binding.etFModelo.setText(f.modelo)
    }

    private fun leerFiltros() = FiltrosCoincidencia(
        pabellon = binding.actFPabellon.text.toString(),
        piso = binding.actFPiso.text.toString(),
        aula = binding.etFAula.text.toString(),
        categoria = binding.actFCategoria.text.toString(),
        color = binding.actFColor.text.toString(),
        marca = binding.etFMarca.text.toString(),
        modelo = binding.etFModelo.text.toString()
    )

    companion object {
        const val EXTRA_ID_REPORTE = "reporteId"

        fun crearIntent(
            contexto: Context,
            reporteId: String,
            usuarioId: String,
            rol: LoginRole
        ): Intent = Intent(contexto, BuscarCoincidenciasActivity::class.java)
            .putExtra(EXTRA_ID_REPORTE, reporteId)
            .putExtra(RegistrarPerdidaActivity.EXTRA_USUARIO_ID, usuarioId)
            .putExtra(RegistrarPerdidaActivity.EXTRA_ROL, rol.code)
    }
}
