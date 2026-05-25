package com.uberfilter.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.uberfilter.ui.LoginResult
import com.uberfilter.ui.LoginViewModel
import com.uberfilter.ui.theme.*

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val result by vm.loginResult.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val passwordFocus = remember { FocusRequester() }

    // ── Navega após login bem-sucedido ────────────────────────────────────────
    LaunchedEffect(result) {
        when (result) {
            is LoginResult.Success -> {
                isLoading = false
                onLoggedIn()
            }
            is LoginResult.Error  -> { isLoading = false }
            null                  -> { /* aguardando */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo / App Name ───────────────────────────────────────────
            Text(
                text = "DriverIQ",
                color = WarmYellow,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Filtro inteligente para motoristas",
                color = WarmOnSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(40.dp))

            // ── Card de login ─────────────────────────────────────────────
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
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // ── Título do card ────────────────────────────────────
                    Text(
                        text = "Entrar",
                        color = WarmOnBg,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // ── Email ─────────────────────────────────────────────
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            vm.clearLoginResult()
                        },
                        label = { Text("Email", color = WarmPlaceholder) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Email,
                                contentDescription = null,
                                tint = WarmPlaceholder
                            )
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

                    // ── Senha ─────────────────────────────────────────────
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            vm.clearLoginResult()
                        },
                        label = { Text("Senha", color = WarmPlaceholder) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = WarmPlaceholder
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Outlined.Visibility
                                    else
                                        Icons.Outlined.VisibilityOff,
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
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    isLoading = true
                                    vm.login(email, password)
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocus),
                        shape = MaterialTheme.shapes.medium,
                        colors = loginFieldColors()
                    )

                    // ── Mensagem de erro ──────────────────────────────────
                    val errorMsg = (result as? LoginResult.Error)?.message
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

                    // ── Botão Entrar ──────────────────────────────────────
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            isLoading = true
                            vm.login(email, password)
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
                                text = "Entrar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = OnWarmYellow
                            )
                        }
                    }

                    // ── Link Criar conta ──────────────────────────────────
                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            text = "Não tem conta? Criar conta",
                            color = WarmYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ── Footer sutil ──────────────────────────────────────────────────
        Text(
            text = "v1.0",
            color = WarmPlaceholder.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor     = WarmOnBg,
    unfocusedTextColor   = WarmOnBg,
    cursorColor          = WarmYellow,
    focusedBorderColor   = WarmYellow,
    unfocusedBorderColor = WarmOutline,
    focusedLabelColor    = WarmYellow,
    unfocusedLabelColor  = WarmPlaceholder,
    focusedContainerColor = PureWhite,
    unfocusedContainerColor = PureWhite,
    focusedLeadingIconColor = WarmYellow,
    unfocusedLeadingIconColor = WarmPlaceholder
)
