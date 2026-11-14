package com.Jair.practica.viewmodel.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.concurrent.Executors
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.repository.AuthRepository

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class InvalidCredentials(val detalle: String = "Credenciales rechazadas") : LoginUiState
    data class Authenticated(val role: LoginRole) : LoginUiState
}

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState
    private val executor = Executors.newSingleThreadExecutor()

    fun signIn(role: LoginRole, username: String, password: String) {
        _uiState.value = LoginUiState.Loading
        executor.execute {
            val authenticated = authRepository.authenticate(role, username, password)
            val resultado = if (authenticated) LoginUiState.Authenticated(role)
            else LoginUiState.InvalidCredentials(
                (authRepository as? com.Jair.practica.model.repository.SupabaseAuthRepository)?.ultimoError
                    ?.takeIf { it.isNotBlank() } ?: "Credenciales rechazadas"
            )
            _uiState.postValue(resultado)
        }
    }

    override fun onCleared() { executor.shutdownNow() }

    fun clearResult() {
        _uiState.value = LoginUiState.Idle
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(LoginViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository) as T
        }
    }
}
