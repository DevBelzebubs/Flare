package com.social.flare.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.flare.features.auth.domain.repository.AuthRepository
import com.social.flare.features.auth.domain.usecase.LoginUseCase
import com.social.flare.features.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    fun login(username: String,
              pass: String,
              onSuccess: () -> Unit,
              onError: (String) -> Unit = {}){
        viewModelScope.launch {
            val result = loginUseCase(username, pass)
            if (result.isSuccess){
                onSuccess()
            }
        }
    }
    fun registerUser(displayName: String,
                     username: String,
                     email: String,
                     pass: String,
                     onSuccess: () -> Unit,
                     onError: (String) -> Unit = {}){
        viewModelScope.launch {
            val result = registerUseCase.invoke(displayName,username,email,pass)
            if (result.isSuccess) {
                val generatedId = result.getOrNull()
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar usuario"
                onError(errorMessage)
            }
        }
    }
}