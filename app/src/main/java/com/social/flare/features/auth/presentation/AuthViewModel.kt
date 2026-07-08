package com.social.flare.features.auth.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.social.flare.FlareApp
import com.social.flare.features.auth.data.repository.AuthRepositoryImpl
import com.social.flare.features.auth.domain.repository.AuthRepository
import com.social.flare.features.auth.domain.usecase.LoginUseCase
import com.social.flare.features.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String,
              pass: String,
              onSuccess: (String) -> Unit,
              onError: (String) -> Unit = {}){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginUseCase(username, pass)
            if (result.isSuccess){
                val tokenOrId = result.getOrNull() ?: ""
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess(tokenOrId)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMessage) }
                onError(errorMessage)
            }
        }
    }
    fun registerUser(
        displayName: String,
        username: String,
        email: String,
        pass: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = registerUseCase.invoke(displayName, username, email, pass)
            if (result.isSuccess) {
                val generatedId = result.getOrNull() ?: ""
                _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
                onSuccess(generatedId)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar usuario"
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMessage) }
                onError(errorMessage)
            }
        }
    }
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearState() {
        _uiState.update { AuthUiState() }
    }

    class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                val app = context.applicationContext as FlareApp
                val repository = AuthRepositoryImpl(app.database.citizenDao(), app.supabase)
                return AuthViewModel(
                    loginUseCase = LoginUseCase(repository),
                    registerUseCase = RegisterUseCase(repository)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}