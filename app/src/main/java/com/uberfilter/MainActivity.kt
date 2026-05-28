package com.uberfilter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.GpsOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.uberfilter.model.GeofenceEntry
import com.uberfilter.service.AppForegroundService
import com.uberfilter.ui.FinanceViewModel
import com.uberfilter.ui.HomeViewModel
import com.uberfilter.ui.LoginViewModel
import com.uberfilter.ui.SettingsViewModel
import com.uberfilter.ui.screens.auth.LoginScreen
import com.uberfilter.ui.screens.auth.RegisterScreen
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

        setContent {
            UberFilterTheme {
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

        // ── Status das permissões (fixo, fora das abas) ─────────────────────────
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

        // ── Abas: Corrida | Destinos ────────────────────────────────────────────
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabTitles = listOf("Corrida", "Destinos")
        val tabIcons = listOf(Icons.Outlined.Tune, Icons.Outlined.GpsOff)

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PureWhite,
            contentColor = WarmYellow,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = WarmYellow
                )
            },
            divider = { HorizontalDivider(color = WarmOutline.copy(alpha = 0.3f), thickness = 0.5.dp) }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    icon = {
                        Icon(
                            tabIcons[index],
                            contentDescription = title,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    selectedContentColor = WarmYellow,
                    unselectedContentColor = WarmOnSurfaceVariant
                )
            }
        }

        // ── Conteúdo da aba ─────────────────────────────────────────────────────
        when (selectedTab) {
            0 -> CorridaTab(
                vm = vm,
                criteria = criteria,
                minValue = minValue, onMinValueChange = { minValue = it },
                minPerKm = minPerKm, onMinPerKmChange = { minPerKm = it },
                minPerHour = minPerHour, onMinPerHourChange = { minPerHour = it },
                minRating = minRating, onMinRatingChange = { minRating = it },
                maxPickupKm = maxPickupKm, onMaxPickupKmChange = { maxPickupKm = it },
                maxPickupMin = maxPickupMin, onMaxPickupMinChange = { maxPickupMin = it },
                minTripKm = minTripKm, onMinTripKmChange = { minTripKm = it },
                maxTripMin = maxTripMin, onMaxTripMinChange = { maxTripMin = it }
            )
            1 -> DestinosTab(vm = vm)
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Aba: Critérios de Corrida ──────────────────────────────────────────────────

@Composable
private fun CorridaTab(
    vm: SettingsViewModel,
    criteria: FilterCriteria,
    minValue: String, onMinValueChange: (String) -> Unit,
    minPerKm: String, onMinPerKmChange: (String) -> Unit,
    minPerHour: String, onMinPerHourChange: (String) -> Unit,
    minRating: String, onMinRatingChange: (String) -> Unit,
    maxPickupKm: String, onMaxPickupKmChange: (String) -> Unit,
    maxPickupMin: String, onMaxPickupMinChange: (String) -> Unit,
    minTripKm: String, onMinTripKmChange: (String) -> Unit,
    maxTripMin: String, onMaxTripMinChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Critérios de Corrida")

        CriteriaField("Valor mínimo total (R$)", minValue)        { onMinValueChange(it) }
        CriteriaField("Receita mínima por km (R$/km)", minPerKm)  { onMinPerKmChange(it) }
        CriteriaField("Receita mínima por hora (R$/h)", minPerHour) { onMinPerHourChange(it) }
        CriteriaField("Avaliação mínima do passageiro", minRating) { onMinRatingChange(it) }
        CriteriaField("Distância máxima até passageiro (km)", maxPickupKm) { onMaxPickupKmChange(it) }
        CriteriaField("Tempo máximo até passageiro (min)", maxPickupMin)   { onMaxPickupMinChange(it) }
        CriteriaField("Distância mínima da viagem (km)", minTripKm)        { onMinTripKmChange(it) }
        CriteriaField("Duração máxima da viagem (min)", maxTripMin)        { onMaxTripMinChange(it) }

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
    }
}

// ── Aba: Destinos Indesejados ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinosTab(vm: SettingsViewModel) {
    val blockedLocations by vm.blockedLocations.collectAsState()
    val geofences by vm.geofences.collectAsState()
    var innerTab by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Sub-abas: Texto | Raio
        TabRow(
            selectedTabIndex = innerTab,
            containerColor = PureWhite,
            contentColor = WarmYellow,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[innerTab]),
                    color = WarmYellow
                )
            },
            divider = { HorizontalDivider(color = WarmOutline.copy(alpha = 0.3f), thickness = 0.5.dp) }
        ) {
            Tab(
                selected = innerTab == 0,
                onClick = { innerTab = 0 },
                text = { Text("Texto", fontWeight = if (innerTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
                selectedContentColor = WarmYellow,
                unselectedContentColor = WarmOnSurfaceVariant
            )
            Tab(
                selected = innerTab == 1,
                onClick = { innerTab = 1 },
                text = { Text("Raio", fontWeight = if (innerTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
                selectedContentColor = WarmYellow,
                unselectedContentColor = WarmOnSurfaceVariant
            )
        }

        when (innerTab) {
            0 -> TextoTab(vm, blockedLocations)
            1 -> RaioTab(vm, geofences)
        }
    }
}

// ── Sub-aba: Bloqueio textual ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextoTab(vm: SettingsViewModel, blockedLocations: List<String>) {
    var newLocation by remember { mutableStateOf("") }
    var duplicateError by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Locais Indesejados")

        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = LightYellowContainer)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (blockedLocations.isEmpty()) {
                    Text(
                        text = "Nenhum local cadastrado.",
                        color = WarmOnSurfaceVariant,
                        fontSize = 13.sp
                    )
                } else {
                    blockedLocations.forEach { location ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = location,
                                color = WarmOnBg,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { vm.removeBlockedLocation(location) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Remover",
                                    tint = RedFinance,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newLocation,
                        onValueChange = {
                            newLocation = it
                            duplicateError = false
                        },
                        placeholder = {
                            Text("Bairro, rua ou cidade", fontSize = 13.sp, color = WarmPlaceholder)
                        },
                        singleLine = true,
                        isError = duplicateError,
                        supportingText = if (duplicateError) {
                            { Text("Este local já está na lista", color = RedFinance, fontSize = 11.sp) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WarmOnBg,
                            unfocusedTextColor = WarmOnBg,
                            cursorColor = WarmYellow,
                            focusedBorderColor = WarmYellow,
                            unfocusedBorderColor = WarmOutline,
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite
                        )
                    )
                    IconButton(
                        onClick = {
                            val trimmed = newLocation.trim()
                            if (trimmed.isBlank()) return@IconButton
                            val alreadyExists = blockedLocations.any {
                                normalizeForComparison(it) == normalizeForComparison(trimmed)
                            }
                            if (alreadyExists) {
                                duplicateError = true
                                return@IconButton
                            }
                            vm.addBlockedLocation(trimmed)
                            newLocation = ""
                        },
                        enabled = newLocation.isNotBlank(),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Adicionar",
                            tint = if (newLocation.isNotBlank()) WarmYellow else WarmOutline
                        )
                    }
                }
            }
        }

        Text(
            text = "O alerta dispara quando o destino contém o termo cadastrado.",
            color = WarmOnSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

// ── Sub-aba: Bloqueio por raio (mapa osmdroid) ────────────────────────────────

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun RaioTab(vm: SettingsViewModel, geofences: List<GeofenceEntry>) {
    val context = LocalContext.current
    var radiusKm by remember { mutableFloatStateOf(1.5f) }
    var centerLabel by remember { mutableStateOf("") }
    val centerLatLng = remember { mutableStateOf(Pair(-22.8267, -43.0519)) } // default: SG/RJ

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Regiões Bloqueadas")

        var showFullscreen by remember { mutableStateOf(false) }

        // Mapa osmdroid com botão expandir
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            OsmMapView(
                center = centerLatLng.value,
                radiusKm = radiusKm.toDouble(),
                onCenterChanged = { lat, lng ->
                    centerLatLng.value = Pair(lat, lng)
                    try {
                        val geocoder = android.location.Geocoder(context)
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            centerLabel = buildString {
                                if (!addr.thoroughfare.isNullOrBlank()) append(addr.thoroughfare)
                                if (!addr.subLocality.isNullOrBlank()) {
                                    if (isNotEmpty()) append(", ")
                                    append(addr.subLocality)
                                }
                                if (!addr.locality.isNullOrBlank()) {
                                    if (isNotEmpty()) append(" - ")
                                    append(addr.locality)
                                }
                                if (!addr.adminArea.isNullOrBlank()) {
                                    if (isNotEmpty()) append("/")
                                    append(addr.adminArea)
                                }
                            }.ifBlank { "${"%.4f".format(lat)}, ${"%.4f".format(lng)}" }
                        } else {
                            centerLabel = "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                        }
                    } catch (_: Exception) {
                        centerLabel = "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Botão expandir
            IconButton(
                onClick = { showFullscreen = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fullscreen,
                    contentDescription = "Expandir mapa",
                    tint = WarmOnBg,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Slider de raio
        Text(
            text = "Raio: ${"%.1f".format(radiusKm)} km",
            color = WarmOnBg,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = radiusKm,
            onValueChange = { radiusKm = it },
            valueRange = 0.5f..10f,
            steps = 18, // incrementos de 0.5
            colors = SliderDefaults.colors(
                thumbColor = WarmYellow,
                activeTrackColor = WarmYellow,
                inactiveTrackColor = WarmOutline
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Endereço (preenchido via reverse geocoding)
        if (centerLabel.isNotBlank()) {
            Text(
                text = "📍 $centerLabel",
                color = WarmOnSurfaceVariant,
                fontSize = 13.sp
            )
        }

        // Botão adicionar
        Button(
            onClick = {
                vm.addGeofence(
                    GeofenceEntry(
                        centerLat = centerLatLng.value.first,
                        centerLng = centerLatLng.value.second,
                        radiusKm = radiusKm.toDouble(),
                        addressLabel = centerLabel.ifBlank {
                            "${"%.4f".format(centerLatLng.value.first)}, ${"%.4f".format(centerLatLng.value.second)}"
                        }
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = WarmYellow,
                contentColor = OnWarmYellow
            )
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Adicionar Região", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Fullscreen dialog
        if (showFullscreen) {
            Dialog(
                onDismissRequest = { showFullscreen = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Scaffold(
                    containerColor = WarmWhite,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "Região Bloqueada",
                                    color = WarmOnBg,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { showFullscreen = false }) {
                                    Icon(
                                        Icons.Filled.ArrowBack,
                                        contentDescription = "Voltar",
                                        tint = WarmYellow
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { showFullscreen = false }) {
                                    Icon(
                                        Icons.Outlined.CloseFullscreen,
                                        contentDescription = "Recolher",
                                        tint = WarmYellow
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = WarmWhite
                            )
                        )
                    },
                    bottomBar = {
                        Surface(
                            color = PureWhite,
                            shadowElevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Raio: ${"%.1f".format(radiusKm)} km",
                                    color = WarmOnBg,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Slider(
                                    value = radiusKm,
                                    onValueChange = { radiusKm = it },
                                    valueRange = 0.5f..10f,
                                    steps = 18,
                                    colors = SliderDefaults.colors(
                                        thumbColor = WarmYellow,
                                        activeTrackColor = WarmYellow,
                                        inactiveTrackColor = WarmOutline
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (centerLabel.isNotBlank()) {
                                    Text(
                                        text = "📍 $centerLabel",
                                        color = WarmOnSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                                Button(
                                    onClick = {
                                        vm.addGeofence(
                                            GeofenceEntry(
                                                centerLat = centerLatLng.value.first,
                                                centerLng = centerLatLng.value.second,
                                                radiusKm = radiusKm.toDouble(),
                                                addressLabel = centerLabel.ifBlank {
                                                    "${"%.4f".format(centerLatLng.value.first)}, ${"%.4f".format(centerLatLng.value.second)}"
                                                }
                                            )
                                        )
                                        showFullscreen = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WarmYellow,
                                        contentColor = OnWarmYellow
                                    )
                                ) {
                                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Confirmar Região", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                ) { padding ->
                    OsmMapView(
                        center = centerLatLng.value,
                        radiusKm = radiusKm.toDouble(),
                        onCenterChanged = { lat, lng ->
                            centerLatLng.value = Pair(lat, lng)
                            try {
                                val geocoder = android.location.Geocoder(context)
                                val addresses = geocoder.getFromLocation(lat, lng, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val addr = addresses[0]
                                    centerLabel = buildString {
                                        if (!addr.thoroughfare.isNullOrBlank()) append(addr.thoroughfare)
                                        if (!addr.subLocality.isNullOrBlank()) {
                                            if (isNotEmpty()) append(", ")
                                            append(addr.subLocality)
                                        }
                                        if (!addr.locality.isNullOrBlank()) {
                                            if (isNotEmpty()) append(" - ")
                                            append(addr.locality)
                                        }
                                        if (!addr.adminArea.isNullOrBlank()) {
                                            if (isNotEmpty()) append("/")
                                            append(addr.adminArea)
                                        }
                                    }.ifBlank { "${"%.4f".format(lat)}, ${"%.4f".format(lng)}" }
                                } else {
                                    centerLabel = "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                                }
                            } catch (_: Exception) {
                                centerLabel = "${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                            }
                        },
                        modifier = Modifier.fillMaxSize().padding(padding)
                    )
                }
            }
        }

        // Lista de geofences cadastrados
        if (geofences.isNotEmpty()) {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = LightYellowContainer)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    geofences.forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.addressLabel,
                                    color = WarmOnBg,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${"%.1f".format(entry.radiusKm)} km",
                                    color = WarmOnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(
                                onClick = { vm.removeGeofence(entry) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Remover",
                                    tint = RedFinance,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Componente: Mapa osmdroid via AndroidView ──────────────────────────────────

@Composable
private fun OsmMapView(
    center: Pair<Double, Double>,
    radiusKm: Double,
    onCenterChanged: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentOnCenterChanged by rememberUpdatedState(onCenterChanged)

    AndroidView(
        factory = { ctx ->
            org.osmdroid.config.Configuration.getInstance().apply {
                userAgentValue = ctx.packageName
                osmdroidTileCache = ctx.cacheDir
            }
            org.osmdroid.views.MapView(ctx).apply {
                clipChildren = true
                setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                // Botões de zoom visíveis
                zoomController.setVisibility(
                    org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS
                )

                // Zoom inicial (executado uma única vez)
                val initialCenter = org.osmdroid.util.GeoPoint(center.first, center.second)
                controller.setCenter(initialCenter)
                controller.setZoom(15.0)

                // Listener de scroll — registrado uma única vez
                addMapListener(object : org.osmdroid.events.MapListener {
                    override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                        event ?: return false
                        val newCenter = mapCenter
                        currentOnCenterChanged(newCenter.latitude, newCenter.longitude)
                        return false
                    }
                    override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean = false
                })
            }
        },
        modifier = modifier.clipToBounds(),
        update = { mapView ->
            val mapCenter = org.osmdroid.util.GeoPoint(center.first, center.second)
            mapView.controller.setCenter(mapCenter)

            // Atualiza overlay: círculo + marcador central
            mapView.overlays.clear()

            // Círculo do raio
            val circle = org.osmdroid.views.overlay.Polygon()
            circle.points = org.osmdroid.views.overlay.Polygon.pointsAsCircle(mapCenter, radiusKm * 1000.0)
            circle.fillPaint.apply {
                color = android.graphics.Color.argb(40, 249, 168, 37)
                style = android.graphics.Paint.Style.FILL
            }
            circle.outlinePaint.apply {
                color = android.graphics.Color.argb(200, 249, 168, 37)
                strokeWidth = 2f
                style = android.graphics.Paint.Style.STROKE
            }
            mapView.overlays.add(circle)

            // Marcador central
            val marker = org.osmdroid.views.overlay.Marker(mapView)
            marker.position = mapCenter
            marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
            marker.title = "Centro"
            mapView.overlays.add(marker)

            mapView.invalidate()
        }
    )
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

/** Normaliza string para comparação: remove acentos, lowercase, trim */
private fun normalizeForComparison(text: String): String {
    val stripped = java.text.Normalizer.normalize(text.trim(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    return stripped.lowercase()
}
