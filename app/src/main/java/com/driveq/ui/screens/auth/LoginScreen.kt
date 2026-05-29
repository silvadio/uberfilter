package com.driveq.ui.screens.auth

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveq.R
import com.driveq.ui.GoogleSignInUiResult
import com.driveq.ui.LoginResult
import com.driveq.ui.LoginViewModel
import com.driveq.ui.theme.*

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val loginResult by vm.loginResult.collectAsState()
    val googleResult by vm.googleSignInResult.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val passwordFocus = remember { FocusRequester() }
    val context = LocalContext.current

    // ── Navega após login (email/senha) bem-sucedido ────────────────────────
    LaunchedEffect(loginResult) {
        when (loginResult) {
            is LoginResult.Success -> {
                isLoading = false
                onLoggedIn()
            }
            is LoginResult.Error  -> { isLoading = false }
            null                  -> { /* aguardando */ }
        }
    }

    // ── Navega após login Google bem-sucedido ───────────────────────────────
    LaunchedEffect(googleResult) {
        when (googleResult) {
            is GoogleSignInUiResult.Success -> {
                isGoogleLoading = false
                onLoggedIn()
            }
            is GoogleSignInUiResult.Error  -> { isGoogleLoading = false }
            null                           -> { /* aguardando */ }
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
                text = "DriveQ",
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

                    // ── Mensagem de erro (email/senha) ──────────────────
                    val emailError = (loginResult as? LoginResult.Error)?.message
                    if (emailError != null) {
                        Text(
                            text = emailError,
                            color = RedFinance,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // ── Botão Entrar (email/senha) ──────────────────────
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
                        enabled = !isLoading && !isGoogleLoading
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

                    // ── Divisor ──────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = WarmOutline.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                        Text(
                            "ou",
                            color = WarmPlaceholder,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = WarmOutline.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }

                    // ── Mensagem de erro (Google) ───────────────────────
                    val googleError = (googleResult as? GoogleSignInUiResult.Error)?.message
                    if (googleError != null) {
                        Text(
                            text = googleError,
                            color = RedFinance,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── Botão Entrar com Google ──────────────────────────
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            isGoogleLoading = true
                            vm.clearGoogleSignInResult()
                            vm.requestGoogleSignIn(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = PureWhite,
                            contentColor = WarmOnBg
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(WarmOutline.copy(alpha = 0.5f))
                        ),
                        enabled = !isLoading && !isGoogleLoading
                    ) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = WarmYellow,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            // Google "G" oficial
                            Image(
                                painter = painterResource(R.drawable.google_icon),
                                contentDescription = "Google",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Entrar com Google",
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = WarmOnBg
                            )
                        }
                    }

                    // ── Link Criar conta ────────────────────────────────
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
