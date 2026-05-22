package com.uberfilter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.uberfilter.model.FilterCriteria
import com.uberfilter.service.AppForegroundService
import com.uberfilter.ui.FinanceViewModel
import com.uberfilter.ui.HomeViewModel
import com.uberfilter.ui.SettingsViewModel
import com.uberfilter.ui.screens.finance.AddTransactionScreen
import com.uberfilter.ui.screens.finance.FinanceScreen
import com.uberfilter.ui.screens.finance.GoalHistoryScreen
import com.uberfilter.ui.screens.finance.TransactionHistoryScreen
import com.uberfilter.ui.screens.home.HomeScreen
import com.uberfilter.ui.theme.*

class MainActivity : ComponentActivity() {

    private val settingsVm: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Status bar na cor do header ─────────────────────────────────────
        window.statusBarColor = android.graphics.Color.parseColor("#FFF9E6")
        androidx.core.view.WindowInsetsControllerCompat(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = true

        AppForegroundService.start(this)

        setContent {
            UberFilterTheme {
                val financeVm: FinanceViewModel = viewModel()
                val homeVm: HomeViewModel = viewModel()
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showFab = currentRoute == "finance"
                val showBottomBar = currentRoute in listOf("home", "filters", "finance")

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
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(vm = homeVm, financeVm = financeVm, settingsVm = settingsVm)
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

// ── Tab model ──────────────────────────────────────────────────────────────────

private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// ── Tela de Filtros ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(vm: SettingsViewModel) {
    val context = LocalContext.current
    val criteria by vm.criteria.collectAsState()

    var minValue     by remember(criteria) { mutableStateOf(criteria.minTotalValue.toString()) }
    var minPerKm     by remember(criteria) { mutableStateOf(criteria.minValuePerKm.toString()) }
    var minPerHour   by remember(criteria) { mutableStateOf(criteria.minValuePerHour.toString()) }
    var minRating    by remember(criteria) { mutableStateOf(criteria.minPassengerRating.toString()) }
    var maxPickupKm  by remember(criteria) { mutableStateOf(criteria.maxPickupDistanceKm.toString()) }
    var maxPickupMin by remember(criteria) { mutableStateOf(criteria.maxPickupMinutes.toString()) }
    var minTripKm    by remember(criteria) { mutableStateOf(criteria.minTripDistanceKm.toString()) }
    var maxTripMin   by remember(criteria) { mutableStateOf(criteria.maxTripDurationMin.toString()) }

    var hasOverlay       by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasAccessibility by remember { mutableStateOf(isAccessibilityEnabled(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlay       = Settings.canDrawOverlays(context)
                hasAccessibility = isAccessibilityEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Título ──────────────────────────────────────────────────────────────
        Text(
            text = "Assistente",
            color = WarmYellow,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        // ── Status das permissões ───────────────────────────────────────────────
        SectionTitle("Permissões")

        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = LightYellowContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PermissionRow(
                    label = "Sobrepor apps",
                    granted = hasOverlay
                ) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }

                PermissionRow(
                    label = "Acessibilidade",
                    granted = hasAccessibility
                ) {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            }
        }

        val allGood = hasOverlay && hasAccessibility
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (allGood) GreenGood.copy(alpha = 0.20f) else RedBad.copy(alpha = 0.20f)
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (allGood) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = if (allGood) GreenFinance else RedFinance
                )
                Text(
                    text = if (allGood) "App pronto para monitorar corridas!"
                           else "Conceda as permissões acima para o app funcionar.",
                    color = WarmOnBg,
                    fontSize = 13.sp
                )
            }
        }

        HorizontalDivider(color = WarmOutline.copy(alpha = 0.4f), thickness = 0.5.dp)

        // ── Critérios configuráveis ─────────────────────────────────────────────
        SectionTitle("Critérios de Corrida")

        CriteriaField("Valor mínimo total (R$)", minValue)        { minValue = it }
        CriteriaField("Receita mínima por km (R$/km)", minPerKm)  { minPerKm = it }
        CriteriaField("Receita mínima por hora (R$/h)", minPerHour) { minPerHour = it }
        CriteriaField("Avaliação mínima do passageiro", minRating) { minRating = it }
        CriteriaField("Distância máxima até passageiro (km)", maxPickupKm) { maxPickupKm = it }
        CriteriaField("Tempo máximo até passageiro (min)", maxPickupMin)   { maxPickupMin = it }
        CriteriaField("Distância mínima da viagem (km)", minTripKm)        { minTripKm = it }
        CriteriaField("Duração máxima da viagem (min)", maxTripMin)        { maxTripMin = it }

        Button(
            onClick = {
                vm.save(
                    FilterCriteria(
                        minTotalValue       = minValue.toDoubleOrNull()    ?: criteria.minTotalValue,
                        minValuePerKm       = minPerKm.toDoubleOrNull()    ?: criteria.minValuePerKm,
                        minValuePerHour     = minPerHour.toDoubleOrNull()  ?: criteria.minValuePerHour,
                        minPassengerRating  = minRating.toDoubleOrNull()   ?: criteria.minPassengerRating,
                        maxPickupDistanceKm = maxPickupKm.toDoubleOrNull() ?: criteria.maxPickupDistanceKm,
                        maxPickupMinutes    = maxPickupMin.toIntOrNull()   ?: criteria.maxPickupMinutes,
                        minTripDistanceKm   = minTripKm.toDoubleOrNull()   ?: criteria.minTripDistanceKm,
                        maxTripDurationMin  = maxTripMin.toIntOrNull()     ?: criteria.maxTripDurationMin
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = WarmYellow,
                contentColor = OnWarmYellow
            )
        ) {
            Icon(Icons.Filled.Save, contentDescription = null, tint = OnWarmYellow)
            Spacer(Modifier.width(10.dp))
            Text(
                "Salvar Critérios",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = OnWarmYellow
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Componentes utilitários ────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        color = WarmOnSurfaceVariant,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun PermissionRow(label: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f), color = WarmOnBg)
        if (granted) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = "Concedido",
                tint = WarmYellow,
                modifier = Modifier.size(20.dp)
            )
        } else {
            TextButton(onClick = onClick) {
                Text("Conceder", color = WarmYellow, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CriteriaField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp, color = WarmPlaceholder) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = WarmOnBg,
            unfocusedTextColor = WarmOnBg,
            cursorColor = WarmYellow,
            focusedBorderColor = WarmYellow,
            unfocusedBorderColor = WarmOutline,
            focusedLabelColor = WarmYellow,
            unfocusedLabelColor = WarmPlaceholder,
            focusedContainerColor = PureWhite,
            unfocusedContainerColor = PureWhite
        )
    )
}

private fun isAccessibilityEnabled(context: android.content.Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(context.packageName, ignoreCase = true)
}
