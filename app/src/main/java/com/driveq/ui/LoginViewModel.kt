package com.driveq.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.driveq.data.FinanceDatabase
import com.driveq.data.GoogleSignInResult
import com.driveq.data.LoginStore
import com.driveq.data.RegisterResult
import com.driveq.data.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    val isGoogleLogin: StateFlow<Boolean> = store.isGoogleLoginFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val loggedPhotoUrl: StateFlow<String?> = store.photoUrlFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult

    private val _registerResult = MutableStateFlow<RegisterUiResult?>(null)
    val registerResult: StateFlow<RegisterUiResult?> = _registerResult

    private val _googleSignInResult = MutableStateFlow<GoogleSignInUiResult?>(null)
    val googleSignInResult: StateFlow<GoogleSignInUiResult?> = _googleSignInResult

    // ── Login email/senha ───────────────────────────────────────────────────

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

    // ── Login com Google ────────────────────────────────────────────────────

    private fun googleSignInClient(context: Context) = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder()
            .requestEmail()
            .requestProfile()
            .build()
    )

    private val _googleSignInLaunch = Channel<Intent>(Channel.BUFFERED)
    val googleSignInLaunch = _googleSignInLaunch.receiveAsFlow()

    fun requestGoogleSignIn(context: Context) {
        _googleSignInLaunch.trySend(googleSignInClient(context).signInIntent)
    }

    fun onGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                processGoogleAccount(account)
            } catch (e: ApiException) {
                _googleSignInResult.value = GoogleSignInUiResult.Error(
                    when (e.statusCode) {
                        12501 -> "Login cancelado."
                        else -> "Falha no login (${e.statusCode}): ${e.message}"
                    }
                )
            } catch (e: Exception) {
                _googleSignInResult.value = GoogleSignInUiResult.Error(
                    "Erro: ${e.message ?: "desconhecido"}"
                )
            }
        }
    }

    fun clearGoogleSignInResult() { _googleSignInResult.value = null }

    fun setGoogleSignInError(message: String) {
        _googleSignInResult.value = GoogleSignInUiResult.Error(message)
    }

    private suspend fun processGoogleAccount(account: GoogleSignInAccount) {
        val googleId = account.id ?: ""
        val name     = account.displayName ?: ""
        val email    = account.email ?: ""
        val photoUrl = account.photoUrl?.toString()

        if (email.isBlank()) {
            _googleSignInResult.value = GoogleSignInUiResult.Error(
                "Não foi possível obter o email da conta Google."
            )
            return
        }

        when (val result = repo.loginWithGoogle(googleId, email, name, photoUrl)) {
            is GoogleSignInResult.Success -> {
                store.setLoggedInWithGoogle(
                    name     = result.user.name,
                    email    = result.user.email,
                    googleId = googleId,
                    photoUrl = photoUrl
                )
                _googleSignInResult.value = GoogleSignInUiResult.Success
            }
            is GoogleSignInResult.Error -> {
                _googleSignInResult.value = GoogleSignInUiResult.Error(result.message)
            }
        }
    }

    // ── Cadastro ────────────────────────────────────────────────────────────

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

    // ── Logout ──────────────────────────────────────────────────────────────

    fun logout() {
        _loginResult.value = null
        _googleSignInResult.value = null
        _registerResult.value = null
        viewModelScope.launch { store.logout() }
    }

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

sealed class GoogleSignInUiResult {
    data object Success : GoogleSignInUiResult()
    data class Error(val message: String) : GoogleSignInUiResult()
}
