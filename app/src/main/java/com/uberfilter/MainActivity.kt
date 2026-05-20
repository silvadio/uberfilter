package com.uberfilter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uberfilter.model.FilterCriteria
import com.uberfilter.service.AppForegroundService
import com.uberfilter.ui.SettingsViewModel
import com.uberfilter.ui.theme.UberFilterTheme

class MainActivity : ComponentActivity() {

    private val vm: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppForegroundService.start(this)

        setContent {
            UberFilterTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen(vm)
                }
            }
        }
    }
}

// ── Tela principal ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: SettingsViewModel) {
    val context = LocalContext.current
    val criteria by vm.criteria.collectAsState()

    // Estados locais para os campos editáveis (string para o TextField)
    var minValue     by remember(criteria) { mutableStateOf(criteria.minTotalValue.toString()) }
    var minPerKm     by remember(criteria) { mutableStateOf(criteria.minValuePerKm.toString()) }
    var minRating    by remember(criteria) { mutableStateOf(criteria.minPassengerRating.toString()) }
    var maxPickupKm  by remember(criteria) { mutableStateOf(criteria.maxPickupDistanceKm.toString()) }
    var maxPickupMin by remember(criteria) { mutableStateOf(criteria.maxPickupMinutes.toString()) }
    var minTripKm    by remember(criteria) { mutableStateOf(criteria.minTripDistanceKm.toString()) }
    var maxTripMin   by remember(criteria) { mutableStateOf(criteria.maxTripDurationMin.toString()) }

    // Permissões ativas?
    val hasOverlay      = Settings.canDrawOverlays(context)
    val hasAccessibility = isAccessibilityEnabled(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UberFilter ⚡", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Status das permissões ──────────────────────────────────────
            SectionTitle("Status das Permissões")

            PermissionRow(
                label = "Exibir sobre outros apps",
                granted = hasOverlay
            ) {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"))
                )
            }

            PermissionRow(
                label = "Serviço de Acessibilidade",
                granted = hasAccessibility
            ) {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }

            // Status geral
            val allGood = hasOverlay && hasAccessibility
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (allGood) Color(0xFF1B8C3E) else Color(0xFFB71C1C)
                )
            ) {
                Text(
                    text = if (allGood) "✅ App pronto para monitorar corridas!"
                           else "⚠️ Conceda as permissões acima para o app funcionar.",
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp
                )
            }

            Divider()

            // ── Critérios configuráveis ────────────────────────────────────
            SectionTitle("Meus Critérios de Corrida")

            CriteriaField("Valor mínimo total (R$)", minValue)        { minValue = it }
            CriteriaField("Receita mínima por km (R$/km)", minPerKm)  { minPerKm = it }
            CriteriaField("Avaliação mínima do passageiro", minRating){ minRating = it }
            CriteriaField("Distância máxima até passageiro (km)", maxPickupKm) { maxPickupKm = it }
            CriteriaField("Tempo máximo até passageiro (min)", maxPickupMin)   { maxPickupMin = it }
            CriteriaField("Distância mínima da viagem (km)", minTripKm)        { minTripKm = it }
            CriteriaField("Duração máxima da viagem (min)", maxTripMin)        { maxTripMin = it }

            Button(
                onClick = {
                    vm.save(FilterCriteria(
                        minTotalValue       = minValue.toDoubleOrNull()    ?: criteria.minTotalValue,
                        minValuePerKm       = minPerKm.toDoubleOrNull()    ?: criteria.minValuePerKm,
                        minPassengerRating  = minRating.toDoubleOrNull()   ?: criteria.minPassengerRating,
                        maxPickupDistanceKm = maxPickupKm.toDoubleOrNull() ?: criteria.maxPickupDistanceKm,
                        maxPickupMinutes    = maxPickupMin.toIntOrNull()   ?: criteria.maxPickupMinutes,
                        minTripDistanceKm   = minTripKm.toDoubleOrNull()   ?: criteria.minTripDistanceKm,
                        maxTripDurationMin  = maxTripMin.toIntOrNull()     ?: criteria.maxTripDurationMin
                    ))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Text("💾  Salvar Critérios", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Componentes utilitários ────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
}

@Composable
private fun PermissionRow(label: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        if (granted) {
            Text("✅", fontSize = 18.sp)
        } else {
            TextButton(onClick = onClick) { Text("Conceder", color = Color(0xFFE53935)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CriteriaField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

// ── Helper: verifica se o serviço de acessibilidade está habilitado ─────────

private fun isAccessibilityEnabled(context: android.content.Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(context.packageName, ignoreCase = true)
}
