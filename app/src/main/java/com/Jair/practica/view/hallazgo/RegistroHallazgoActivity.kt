package com.Jair.practica.view.hallazgo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.viewmodel.hallazgo.RegistroHallazgoViewModel

class RegistroHallazgoActivity : AppCompatActivity() {
    private lateinit var controller: RegistroHallazgoController
    private val viewModel: RegistroHallazgoViewModel by viewModels()

    private val tomarFoto = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        controller.mostrarFoto(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_hallazgo)

        // Recuperamos el rol de la sesión. (Simulando PERSONAL_REGISTRADOR si no viene del Login)
        val rolActualStr = intent.getStringExtra("rol")
            ?: intent.getStringExtra("ROL_USUARIO")
            ?: LoginRole.PERSONAL_REGISTRADOR.code
        val rolActivo = LoginRole.fromCode(rolActualStr)

        // Observar errores de validación
        viewModel.mensajeError.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            controller.reactivarBoton()
        }

        // Observar respuesta exitosa
        viewModel.registroExitoso.observe(this) { objeto ->
            Toast.makeText(this, "Hallazgo registrado. Estado: ${objeto?.estado}", Toast.LENGTH_SHORT).show()

            // Navegamos pasando el ID real generado en la base de datos local
            val intent = android.content.Intent(this, com.Jair.practica.view.login.LoginActivity::class.java)
            intent.putExtra("mensaje", "Hallazgo registrado. El Administrador SAE debe confirmar la recepción.")
            startActivity(intent)
            finish()
        }

        // Inicializamos el controlador pasándole el ViewModel y el rol
        controller = RegistroHallazgoController(
            activity = this,
            viewModel = viewModel,
            rolActivo = rolActivo,
            abrirCamara = { tomarFoto.launch(null) }
        )
        controller.iniciar()
    }
}
