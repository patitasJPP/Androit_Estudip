package com.Jair.practica.view.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.databinding.ActivityLoginBinding
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.repository.DemoAuthRepository
import com.Jair.practica.view.custodia.InventarioActivity
import com.Jair.practica.view.hallazgo.RegistroHallazgoActivity
import com.Jair.practica.viewmodel.login.LoginUiState
import com.Jair.practica.viewmodel.login.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(DemoAuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.uiState.observe(this) { state ->
            when (state) {
                LoginUiState.Idle -> Unit
                LoginUiState.InvalidCredentials -> {
                    viewModel.clearResult()
                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
                is LoginUiState.Authenticated -> {
                    viewModel.clearResult()
                    val destination = when (state.role) {
                        LoginRole.PERSONAL_REGISTRADOR -> RegistroHallazgoActivity::class.java
                        LoginRole.ADMINISTRADOR_SAE -> InventarioActivity::class.java
                    }
                    startActivity(Intent(this, destination))
                    finish()
                }
            }
        }

        binding.imgMostrarClave.setOnClickListener { togglePasswordVisibility() }
        binding.btnLogin.setOnClickListener {
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
