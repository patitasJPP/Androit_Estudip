package com.Jair.practica.view.devolucion

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.LocalAuditoriaRepository
import com.Jair.practica.model.local.LocalEntregaRepository
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.LocalPersonaAtendidaRepository
import com.Jair.practica.model.local.LocalUsuarioRepository
import com.Jair.practica.model.repository.AuditoriaRepository
import com.Jair.practica.model.repository.EntregaRepository
import com.Jair.practica.model.repository.ObjetoEncontradoRepository
import com.Jair.practica.model.repository.PersonaAtendidaRepository
import com.Jair.practica.model.repository.UsuarioRepository
import com.Jair.practica.view.common.NavegacionPrincipal
import com.Jair.practica.viewmodel.devolucion.ActaEntregaUiState
import com.Jair.practica.viewmodel.devolucion.ActaEntregaViewModel

class ActaEntregaActivity : AppCompatActivity() {
    private lateinit var controller: ActaEntregaController
    var idEntrega: String? = null

    private val viewModel: ActaEntregaViewModel by viewModels {
        ActaEntregaViewModel.Factory(
            entregaRepository = LocalEntregaRepository(),
            usuarioRepository = LocalUsuarioRepository(),
            objetoRepository = LocalObjetoEncontradoRepository(),
            personaRepository = LocalPersonaAtendidaRepository(),
            auditoriaRepository = LocalAuditoriaRepository()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idEntrega = intent?.getStringExtra(EXTRA_ID_ENTREGA)
        setContentView(R.layout.activity_acta_entrega)
        NavegacionPrincipal.configurar(this, R.id.rootActaDevolucion)
        controller = ActaEntregaController(this)
        controller.iniciar()
        viewModel.cargar(idEntrega ?: "", LoginRole.fromCode(intent?.getStringExtra("rol")))
        viewModel.state.observe(this) { state -> bindEstado(state) }
    }

    private fun bindEstado(state: ActaEntregaUiState) {
        val errorView = findViewById<TextView>(R.id.tvErrorActa)
        val data = state.data

        if (state.errorCarga != null || state.errorGeneral != null) {
            errorView.visibility = View.VISIBLE
            errorView.text = state.errorCarga ?: state.errorGeneral
        } else {
            errorView.visibility = View.GONE
        }

        val entrega = data?.entrega ?: return

        findViewById<TextView>(R.id.tvEstadoCerrada).text = ActaEntregaViewModel.etiquetaEstadoActa(entrega)
        findViewById<TextView>(R.id.tvNumeroCorrelativo).text = ActaEntregaViewModel.correlativoActa(entrega)
        findViewById<TextView>(R.id.tvFechaCierre).text = ActaEntregaViewModel.formatearFechaCierre(entrega.fechaConfirmacion)

        val persona = data.persona
        val objeto = data.objeto
        val administrador = data.administrador

        persona?.let { p ->
            findViewById<TextView>(R.id.tvNombreEstudiante).text = p.nombre
            findViewById<TextView>(R.id.tvCodigoEstudiante).text = p.identificador ?: ""
            findViewById<TextView>(R.id.tvDniEstudiante).text = p.dni ?: ""
            findViewById<TextView>(R.id.tvEstadoMatricula).text = p.vinculoUniversidad
            findViewById<TextView>(R.id.tvSedeInstitucional).text = objeto?.lugarHallazgo ?: ""
            findViewById<TextView>(R.id.tvCorreoUtp).text = p.correo ?: ""
            findViewById<TextView>(R.id.tvMovilValidado).text = p.telefono ?: ""
        }
        findViewById<TextView>(R.id.tvNombreEquipo).text = objeto?.codigoCustodia ?: ""
        findViewById<TextView>(R.id.tvUbicacionHallazgo).text = objeto?.lugarHallazgo ?: ""

        findViewById<ImageView>(R.id.imgFirmaEstudiante).setImageResource(android.R.drawable.ic_menu_edit)
        findViewById<TextView>(R.id.tvNombreFirmaEstudiante).text = persona?.nombre ?: ""
        findViewById<ImageView>(R.id.imgFirmaOficial).setImageResource(android.R.drawable.ic_menu_edit)
        findViewById<TextView>(R.id.tvNombreFirmaOficial).text = administrador?.nombre ?: ""

        findViewById<TextView>(R.id.tvDeclaracionJurada).text =
            "El receptor declara haber recibido a total satisfacción y en idénticas condiciones de funcionalidad el bien detallado, liberando de custodia y responsabilidad patrimonial a la Universidad Tecnológica del Perú según Art. 44 del Reglamento Institucional."
    }

    companion object {
        const val EXTRA_ID_ENTREGA = "entregaId"
    }
}
