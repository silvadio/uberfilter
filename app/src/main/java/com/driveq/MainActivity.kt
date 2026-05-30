package com.driveq

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.GpsOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.driveq.model.FilterCriteria
import com.driveq.service.AppForegroundService
import kotlinx.coroutines.launch
import com.driveq.ui.FinanceViewModel
import com.driveq.ui.HomeViewModel
import com.driveq.ui.LoginViewModel
import com.driveq.ui.SettingsViewModel
import com.driveq.ui.screens.auth.LoginScreen
import com.driveq.ui.screens.auth.RegisterScreen
import com.driveq.ui.screens.finance.AddTransactionScreen
import com.driveq.ui.screens.finance.FinanceScreen
import com.driveq.ui.screens.finance.GoalHistoryScreen
import com.driveq.ui.screens.finance.TransactionHistoryScreen
import com.driveq.ui.screens.destinos.DestinosScreen
import com.driveq.ui.screens.filters.FiltersScreen
import com.driveq.ui.screens.home.HomeScreen
import com.driveq.ui.theme.*

class MainActivity : ComponentActivity() {

    private val settingsVm: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Status bar na cor do header ─────────────────────────────────────
        window.statusBarColor = android.graphics.Color.argb(
            (LightYellowBg.alpha * 255).toInt(),
            (LightYellowBg.red * 255).toInt(),
            (LightYellowBg.green * 255).toInt(),
            (LightYellowBg.blue * 255).toInt()
        )
        androidx.core.view.WindowInsetsControllerCompat(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = true

        AppForegroundService.start(this)

        // ── Google Sign-In launcher ────────────────────────────────────────
        val loginVm: LoginViewModel by viewModels()
        val googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                loginVm.onGoogleSignInResult(result.data)
            } else {
                loginVm.setGoogleSignInError(
                    if (result.resultCode == RESULT_CANCELED) "Login cancelado."
                    else "Login não concluído (código ${result.resultCode})."
                )
            }
        }

        lifecycleScope.launch {
            loginVm.googleSignInLaunch.collect { intent ->
                googleSignInLauncher.launch(intent)
            }
        }

        setContent {
            DriveQTheme {
                val financeVm: FinanceViewModel = viewModel()
                val homeVm: HomeViewModel = viewModel()
                val loginVm: LoginViewModel = viewModel()
                val navController = rememberNavController()

                val isLoggedIn by loginVm.isLoggedIn.collectAsState()
                var authChecked by remember { mutableStateOf(false) }

                LaunchedEffect(isLoggedIn) { authChecked = true }

                if (!authChecked) {
                    // Splash silencioso enquanto verifica sessão
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(WarmWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = WarmYellow, strokeWidth = 3.dp)
                    }
                } else {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                val showFab = currentRoute == "finance"
                val showBottomBar = currentRoute in listOf("home", "filters", "destinos", "finance")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = WarmWhite,
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = PureWhite,
                                contentColor = WarmOnBg,
                                tonalElevation = 0.dp
                            ) {
                                val items = listOf(
                                    TabItem("home", "Home", Icons.Outlined.Home),
                                    TabItem("filters", "Filtros", Icons.Outlined.Tune),
                                    TabItem("destinos", "Destinos", Icons.Outlined.GpsOff),
                                    TabItem("finance", "Finanças", Icons.Outlined.AccountBalanceWallet)
                                )
                                items.forEach { tab ->
                                    val selected = navBackStackEntry?.destination?.hierarchy
                                        ?.any { it.route == tab.route } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(tab.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = {
                                            Text(
                                                tab.label,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = WarmYellow,
                                            selectedTextColor = WarmYellow,
                                            unselectedIconColor = WarmOnSurfaceVariant,
                                            unselectedTextColor = WarmOnSurfaceVariant,
                                            indicatorColor = WarmYellowGlow
                                        )
                                    )
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        if (showFab) {
                            FloatingActionButton(
                                onClick = { navController.navigate("finance/add") },
                                containerColor = WarmYellow,
                                contentColor = OnWarmYellow,
                                shape = MaterialTheme.shapes.large
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Adicionar transação")
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) "home" else "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                vm = loginVm,
                                onLoggedIn = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                vm = loginVm,
                                onRegistered = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                vm = homeVm,
                                financeVm = financeVm,
                                settingsVm = settingsVm,
                                loginVm = loginVm,
                                onLogout = {
                                    loginVm.logout()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("destinos") {
                            DestinosScreen(settingsVm)
                        }

                        composable("filters") {
                            FiltersScreen(settingsVm)
                        }
                        composable("finance") {
                            FinanceScreen(
                                vm = financeVm,
                                onNavigateToHistory = { navController.navigate("finance/goals/history") },
                                onNavigateToTransactions = { navController.navigate("finance/transactions/history") }
                            )
                        }
                        composable("finance/add") {
                            AddTransactionScreen(
                                vm = financeVm,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("finance/goals/history") {
                            GoalHistoryScreen(
                                vm = financeVm,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("finance/transactions/history") {
                            TransactionHistoryScreen(
                                vm = financeVm,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

// ── Tab model ──────────────────────────────────────────────────────────────────

private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
