package com.Jair.practica.view.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.databinding.ActivityLoginBinding
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.model.repository.SupabaseAuthRepository
import com.Jair.practica.view.custodia.InventarioActivity
import com.Jair.practica.view.hallazgo.RegistroHallazgoActivity
import com.Jair.practica.view.common.MenuPrincipalActivity
import com.Jair.practica.view.login.SeleccionRolController
import com.Jair.practica.viewmodel.login.LoginUiState
import com.Jair.practica.viewmodel.login.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(SupabaseAuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.getStringExtra("mensaje")?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                LoginUiState.Idle -> Unit
                LoginUiState.Loading -> binding.btnLogin.isEnabled = false
                is LoginUiState.InvalidCredentials -> {
                    binding.btnLogin.isEnabled = true
                    binding.tvInfoDescripcion.text = state.detalle
                    binding.tvInfoDescripcion.setTextColor(android.graphics.Color.rgb(184, 12, 46))
                    viewModel.clearResult()
                    Toast.makeText(this, state.detalle, Toast.LENGTH_LONG).show()
                }
                is LoginUiState.Authenticated -> {
                    binding.btnLogin.isEnabled = true
                    viewModel.clearResult()
                    val destination = MenuPrincipalActivity::class.java
                    val usuarioId = if (state.role == LoginRole.PERSONAL_REGISTRADOR) {
                        LocalDataStore.USUARIO_REGISTRADOR_ID
                    } else {
                        LocalDataStore.USUARIO_ADMINISTRADOR_ID
                    }
                    startActivity(
                        Intent(this, destination).apply {
                            putExtra("usuarioId", usuarioId)
                            putExtra("rol", state.role.code)
                            putExtra("pendienteId", AlmacenLocal.instancia.ultimoObjetoPendienteId)
                        }
                    )
                    finish()
                }
            }
        }

        binding.imgMostrarClave.setOnClickListener { togglePasswordVisibility() }

        val rolExtra = intent?.getStringExtra(SeleccionRolController.EXTRA_ROL)
        if (rolExtra == LoginRole.PERSONAL_REGISTRADOR.code) {
            binding.rbPersonalRegistrador.isChecked = true
        } else if (rolExtra == LoginRole.ADMINISTRADOR_SAE.code) {
            binding.rbAdministradorSae.isChecked = true
        }

        binding.btnLogin.setOnClickListener {
            binding.tvInfoDescripcion.text = "Validando credenciales institucionales..."
            binding.tvInfoDescripcion.setTextColor(android.graphics.Color.rgb(100, 116, 139))
            val role = when (binding.rgRolLogin.checkedRadioButtonId) {
                binding.rbPersonalRegistrador.id -> LoginRole.PERSONAL_REGISTRADOR
                else -> LoginRole.ADMINISTRADOR_SAE
            }
            viewModel.signIn(
                role,
                binding.etUsuario.text.toString(),
                binding.etContrasena.text.toString()
            )
        }
    }

    private fun togglePasswordVisibility() {
        val password = binding.etContrasena
        val visible = password.inputType ==
            (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
        password.inputType = if (visible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        password.setSelection(password.text.length)
    }
}
