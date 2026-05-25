package com.uberfilter.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uberfilter.ui.LoginViewModel
import com.uberfilter.ui.RegisterUiResult
import com.uberfilter.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    vm: LoginViewModel,
    onRegistered: () -> Unit,
    onBack: () -> Unit
) {
    val result by vm.registerResult.collectAsState()

    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(result) {
        when (result) {
            is RegisterUiResult.Success -> {
                isLoading = false
                onRegistered()
            }
            is RegisterUiResult.Error  -> { isLoading = false }
            null                       -> { /* aguardando */ }
        }
    }

    Scaffold(
        containerColor = WarmWhite,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = WarmYellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmWhite
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Título ───────────────────────────────────────────────────
            Text(
                text = "DriverIQ",
                color = WarmYellow,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Crie sua conta",
                color = WarmOnSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(32.dp))

            // ── Card de cadastro ─────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp), clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.42f),
                        spotColor = Color.Black.copy(alpha = 0.30f))
                    .border(0.5.dp, WarmOutline.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Cadastro",
                        color = WarmOnBg,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // ── Nome ─────────────────────────────────────────────
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            vm.clearRegisterResult()
                        },
                        label = { Text("Nome completo", color = WarmPlaceholder) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Badge, contentDescription = null, tint = WarmPlaceholder)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = loginFieldColors()
                    )

                    // ── Email ─────────────────────────────────────────────
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            vm.clearRegisterResult()
                        },
                        label = { Text("Email", color = WarmPlaceholder) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = WarmPlaceholder)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = loginFieldColors()
                    )

                    // ── Senha ────────────────────────────────────────────
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            vm.clearRegisterResult()
                        },
                        label = { Text("Senha", color = WarmPlaceholder) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = WarmPlaceholder)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility
                                                  else Icons.Outlined.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar senha"
                                                        else "Mostrar senha",
                                    tint = WarmPlaceholder
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = loginFieldColors()
                    )

                    // ── Confirmar senha ──────────────────────────────────
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            vm.clearRegisterResult()
                        },
                        label = { Text("Confirmar senha", color = WarmPlaceholder) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = WarmPlaceholder)
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (name.isNotBlank() && email.isNotBlank() &&
                                    password.isNotBlank() && confirmPassword.isNotBlank()) {
                                    isLoading = true
                                    vm.register(name, email, password, confirmPassword)
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = loginFieldColors()
                    )

                    // ── Erro ─────────────────────────────────────────────
                    val errorMsg = (result as? RegisterUiResult.Error)?.message
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg,
                            color = RedFinance,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // ── Botão ────────────────────────────────────────────
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            isLoading = true
                            vm.register(name, email, password, confirmPassword)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarmYellow,
                            contentColor = OnWarmYellow
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = OnWarmYellow,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Criar conta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = OnWarmYellow
                            )
                        }
                    }
                }
            }
        }
    }
}
