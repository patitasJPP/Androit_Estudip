package com.Jair.practica.view.perdida

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.databinding.ActivityRegistrarPerdidaBinding
import com.Jair.practica.databinding.ItemReportePendienteBinding
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.model.local.AlmacenPerdidas
import com.Jair.practica.model.local.LocalReportePerdidaRepository
import com.Jair.practica.model.perdida.CatalogoPerdidas
import com.Jair.practica.model.perdida.DatosReportePerdida
import com.Jair.practica.model.perdida.ErroresReporte
import com.Jair.practica.model.perdida.etiqueta
import com.Jair.practica.model.perdida.permiteBuscar
import com.Jair.practica.viewmodel.perdida.RegistrarPerdidaUiState
import com.Jair.practica.viewmodel.perdida.RegistrarPerdidaViewModel

class RegistrarPerdidaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarPerdidaBinding

    private val actorId: String by lazy {
        intent.getStringExtra(EXTRA_USUARIO_ID) ?: AlmacenPerdidas.USUARIO_REGISTRADOR_ID
    }
    private val actorRol: LoginRole by lazy { LoginRole.fromCode(intent.getStringExtra(EXTRA_ROL)) }

    private val viewModel: RegistrarPerdidaViewModel by viewModels {
        RegistrarPerdidaViewModel.Factory(LocalReportePerdidaRepository(), actorId, actorRol)
    }

    /** Id del reporte volcado en el formulario; evita pisar lo que el usuario está escribiendo. */
    private var idReporteMostrado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarPerdidaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ajustarInsets(binding.rootRegistrarPerdida)

        binding.actCategoria.cargarOpciones(CatalogoPerdidas.CATEGORIAS)
        binding.actColor.cargarOpciones(CatalogoPerdidas.COLORES)
        binding.actPabellon.cargarOpciones(CatalogoPerdidas.PABELLONES)
        binding.actPiso.cargarOpciones(CatalogoPerdidas.PISOS)

        binding.ivVolver.setOnClickListener { finish() }
        binding.btnRegistrar.setOnClickListener { viewModel.registrar(leerDatos()) }
        binding.btnGuardarBorrador.setOnClickListener { viewModel.guardarBorrador(leerDatos()) }
        binding.btnNuevoReporte.setOnClickListener { viewModel.nuevoReporte() }
        binding.btnBuscarCoincidencias.setOnClickListener {
            val id = viewModel.state.value?.reporteActual?.id ?: return@setOnClickListener
            startActivity(BuscarCoincidenciasActivity.crearIntent(this, id, actorId, actorRol))
        }

        viewModel.state.observe(this) { render(it) }
    }

    override fun onResume() {
        super.onResume()
        // Recarga pendientes y refresca el estado del reporte abierto (pudo cambiar en la búsqueda).
        viewModel.iniciar()
    }

    private fun render(state: RegistrarPerdidaUiState) {
        binding.tvAccesoDenegado.setText(R.string.perdida_acceso_denegado)
        binding.tvAccesoDenegado.visibility = if (state.accesoDenegado) View.VISIBLE else View.GONE
        binding.layoutContenido.visibility = if (state.accesoDenegado) View.GONE else View.VISIBLE
        if (state.accesoDenegado) return

        mostrarPendientes(state.pendientes)
        volcarReporteSiCambio(state.reporteActual)
        mostrarErrores(state.errores)

        val reporte = state.reporteActual
        val esBorradorOnuevo = reporte == null || reporte.estado == EstadoReportePerdida.BORRADOR
        binding.tvEstadoReporte.text = if (reporte == null) {
            getString(R.string.perdida_estado_nuevo)
        } else {
            getString(R.string.perdida_estado_reanudando, reporte.id, reporte.estado.etiqueta())
        }
        establecerEditable(state.formularioEditable)
        // Mientras corre una operación se bloquean los botones (control de doble envío).
        binding.btnRegistrar.isEnabled = state.formularioEditable && !state.cargando
        binding.btnGuardarBorrador.isEnabled = esBorradorOnuevo && !state.cargando
        binding.btnBuscarCoincidencias.visibility =
            if (reporte?.estado?.permiteBuscar() == true) View.VISIBLE else View.GONE
        binding.btnNuevoReporte.visibility = if (reporte != null) View.VISIBLE else View.GONE

        state.mensaje?.let {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            viewModel.mensajeConsumido()
        }
    }

    private fun mostrarPendientes(pendientes: List<ReportePerdida>) {
        binding.cardPendientes.visibility = if (pendientes.isEmpty()) View.GONE else View.VISIBLE
        binding.containerPendientes.removeAllViews()
        pendientes.forEach { reporte ->
            val item = ItemReportePendienteBinding.inflate(
                layoutInflater, binding.containerPendientes, false
            )
            item.tvPendienteDescripcion.text =
                reporte.descripcion.ifBlank { getString(R.string.perdida_pendiente_sin_descripcion) }
            item.tvPendienteEstado.text = "${reporte.id} · ${reporte.estado.etiqueta()}"
            item.btnReanudar.setOnClickListener { viewModel.reanudar(reporte.id) }
            binding.containerPendientes.addView(item.root)
        }
    }

    /** Vuelca el reporte en el formulario solo cuando cambia de reporte (reanudar / nuevo). */
    private fun volcarReporteSiCambio(reporte: ReportePerdida?) {
        if (reporte?.id == idReporteMostrado) return
        idReporteMostrado = reporte?.id
        binding.etDescripcion.setText(reporte?.descripcion.orEmpty())
        binding.etLugar.setText(reporte?.lugarAproximado.orEmpty())
        binding.actCategoria.mostrarValor(reporte?.categoria)
        binding.actColor.mostrarValor(reporte?.color)
        binding.etMarca.setText(reporte?.marca.orEmpty())
        binding.etModelo.setText(reporte?.modelo.orEmpty())
        binding.actPabellon.mostrarValor(reporte?.pabellon)
        binding.actPiso.mostrarValor(reporte?.piso)
        binding.etAula.setText(reporte?.aula.orEmpty())
        binding.etContactoNombre.setText(reporte?.contactoNombre.orEmpty())
        binding.etContactoCorreo.setText(reporte?.contactoCorreo.orEmpty())
        binding.etContactoTelefono.setText(reporte?.contactoTelefono.orEmpty())
    }

    /** Cada mensaje de error aparece junto al campo que lo causó (control de la guía). */
    private fun mostrarErrores(e: ErroresReporte) {
        binding.tilDescripcion.error = e.descripcion
        binding.tilLugar.error = e.lugarAproximado
        binding.tilCategoria.error = e.categoria
        binding.tilColor.error = e.color
        binding.tilMarca.error = e.marca
        binding.tilModelo.error = e.modelo
        binding.tilPabellon.error = e.pabellon
        binding.tilPiso.error = e.piso
        binding.tilAula.error = e.aula
        binding.tilContactoNombre.error = e.contactoNombre
        binding.tilContactoCorreo.error = e.contactoCorreo
        binding.tilContactoTelefono.error = e.contactoTelefono
        binding.tvErrorContacto.text = e.medioContacto
        binding.tvErrorContacto.visibility = if (e.medioContacto != null) View.VISIBLE else View.GONE
    }

    private fun establecerEditable(editable: Boolean) {
        listOf(
            binding.etDescripcion, binding.etLugar, binding.actCategoria, binding.actColor,
            binding.etMarca, binding.etModelo, binding.actPabellon, binding.actPiso, binding.etAula,
            binding.etContactoNombre, binding.etContactoCorreo, binding.etContactoTelefono
        ).forEach { it.isEnabled = editable }
    }

    private fun leerDatos() = DatosReportePerdida(
        descripcion = binding.etDescripcion.text.toString(),
        lugarAproximado = binding.etLugar.text.toString(),
        categoria = binding.actCategoria.text.toString(),
        color = binding.actColor.text.toString(),
        marca = binding.etMarca.text.toString(),
        modelo = binding.etModelo.text.toString(),
        pabellon = binding.actPabellon.text.toString(),
        piso = binding.actPiso.text.toString(),
        aula = binding.etAula.text.toString(),
        contactoNombre = binding.etContactoNombre.text.toString(),
        contactoCorreo = binding.etContactoCorreo.text.toString(),
        contactoTelefono = binding.etContactoTelefono.text.toString()
    )

    companion object {
        /** Mismos nombres de extras que usa el Login para pasar usuario y rol. */
        const val EXTRA_USUARIO_ID = "usuarioId"
        const val EXTRA_ROL = "rol"

        fun crearIntent(
            contexto: Context,
            usuarioId: String = AlmacenPerdidas.USUARIO_REGISTRADOR_ID,
            rol: LoginRole = LoginRole.PERSONAL_REGISTRADOR
        ): Intent = Intent(contexto, RegistrarPerdidaActivity::class.java)
            .putExtra(EXTRA_USUARIO_ID, usuarioId)
            .putExtra(EXTRA_ROL, rol.code)
    }
}
