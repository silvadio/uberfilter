package com.uberfilter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uberfilter.data.FinanceDatabase
import com.uberfilter.data.LoginStore
import com.uberfilter.data.RegisterResult
import com.uberfilter.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val repo  = UserRepository(FinanceDatabase.getInstance(app).userDao())
    private val store = LoginStore(app)

    val isLoggedIn: StateFlow<Boolean> = store.isLoggedInFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val loggedEmail: StateFlow<String?> = store.loggedEmailFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val loggedName: StateFlow<String?> = store.loggedNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    /** Resultado da última tentativa de login */
    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult

    /** Resultado da última tentativa de cadastro */
    private val _registerResult = MutableStateFlow<RegisterUiResult?>(null)
    val registerResult: StateFlow<RegisterUiResult?> = _registerResult

    // ── Login ────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = LoginResult.Error("Preencha email e senha.")
            return
        }
        viewModelScope.launch {
            val user = repo.login(email.trim(), password.trim())
            if (user != null) {
                store.setLoggedIn(user.name, user.email)
                _loginResult.value = LoginResult.Success
            } else {
                _loginResult.value = LoginResult.Error("Email ou senha inválidos.")
            }
        }
    }

    fun clearLoginResult() { _loginResult.value = null }

    // ── Cadastro ─────────────────────────────────────────────────────────

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _registerResult.value = RegisterUiResult.Error("Preencha todos os campos.")
            return
        }
        if (!isValidEmail(email.trim())) {
            _registerResult.value = RegisterUiResult.Error("Informe um email válido.")
            return
        }
        if (password != confirmPassword) {
            _registerResult.value = RegisterUiResult.Error("As senhas não conferem.")
            return
        }
        if (password.length < 4) {
            _registerResult.value = RegisterUiResult.Error("A senha deve ter ao menos 4 caracteres.")
            return
        }
        viewModelScope.launch {
            when (val result = repo.register(name.trim(), email.trim(), password)) {
                is RegisterResult.Success -> {
                    store.setLoggedIn(result.user.name, result.user.email)
                    _registerResult.value = RegisterUiResult.Success
                }
                is RegisterResult.EmailAlreadyUsed -> {
                    _registerResult.value = RegisterUiResult.Error("Este email já está cadastrado.")
                }
            }
        }
    }

    fun clearRegisterResult() { _registerResult.value = null }

    // ── Logout ───────────────────────────────────────────────────────────

    fun logout() {
        viewModelScope.launch { store.logout() }
    }

    // ── Validação ────────────────────────────────────────────────────────

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

sealed class LoginResult {
    data object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}

sealed class RegisterUiResult {
    data object Success : RegisterUiResult()
    data class Error(val message: String) : RegisterUiResult()
}
