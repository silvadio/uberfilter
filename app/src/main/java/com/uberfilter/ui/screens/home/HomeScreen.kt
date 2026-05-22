package com.uberfilter.ui.screens.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uberfilter.ui.FinanceViewModel
import com.uberfilter.ui.HomeViewModel
import com.uberfilter.ui.SettingsViewModel
import com.uberfilter.ui.components.GoalCard
import com.uberfilter.ui.components.GoalDialog
import com.uberfilter.ui.components.PeriodSelector
import com.uberfilter.ui.components.PremiumHeader
import com.uberfilter.ui.theme.*

@Composable
fun HomeScreen(vm: HomeViewModel, financeVm: FinanceViewModel, settingsVm: SettingsViewModel) {
    val uiState by vm.uiState.collectAsState()
    val selectedPeriod by vm.selectedPeriod.collectAsState()
    val goal by financeVm.goal.collectAsState()
    val goalProgress by financeVm.goalProgress.collectAsState()
    val assistantEnabled by settingsVm.assistantEnabled.collectAsState()
    val weeklyBalance by financeVm.weeklyBalance.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }

    if (showGoalDialog) {
        GoalDialog(
            currentGoal = goal,
            onDismiss = { showGoalDialog = false },
            onSave = { type, amount -> financeVm.setGoal(type, amount) },
            onClear = { financeVm.clearGoal() }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PremiumHeader(
            greetingName = "Morris",
            subtitle = "Bem-vindo ao DriverIQ",
            avatarInitials = "MO",
            onSettingsClick = { /* TODO: navegar para configurações */ },
            onAvatarClick = { /* TODO: navegar para perfil */ }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

        // ── Switch assistente ───────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.42f), spotColor = Color.Black.copy(alpha = 0.30f))
                .border(0.5.dp, WarmOutline.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (assistantEnabled) LightYellowContainer else PureWhite
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrackChanges,
                        contentDescription = null,
                        tint = if (assistantEnabled) WarmYellow else WarmOnSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Assistente de corrida",
                            color = WarmOnBg,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (assistantEnabled) "Ativo — avaliando ofertas"
                                   else "Inativo — popup desligado",
                            color = if (assistantEnabled) WarmYellow else WarmOnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = assistantEnabled,
                    onCheckedChange = { settingsVm.setAssistantEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = WarmYellow,
                        checkedTrackColor = WarmYellowGlow,
                        uncheckedThumbColor = WarmOnSurfaceVariant,
                        uncheckedTrackColor = WarmOutline.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // ── Saldo da semana ────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.42f), spotColor = Color.Black.copy(alpha = 0.30f))
                .border(0.5.dp, WarmOutline.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        tint = WarmYellow,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Saldo Líquido / Semana",
                        color = WarmOnBg,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "R$ ${"%.2f".format(weeklyBalance)}",
                    color = WarmOnBg,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ── Cards de classificação ──────────────────────────────────────────────
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = WarmYellow,
                    strokeWidth = 3.dp
                )
            }
        } else {
            val stats = uiState.stats

            if (stats != null) {
                val total = stats.redCount + stats.yellowCount + stats.greenCount

                // ── Card único com total + 3 stats ─────────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.48f), spotColor = Color.Black.copy(alpha = 0.35f))
                        .border(0.5.dp, WarmOutline.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "$total corrida${if (total != 1) "s" else ""} avaliada${if (total != 1) "s" else ""}",
                            color = WarmOnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )

                        PeriodSelector(
                            selected = selectedPeriod,
                            onSelect = { vm.setPeriod(it) }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatsCard(
                                label = "Ruins",
                                count = stats.redCount,
                                pct = stats.redPct,
                                containerColor = RedBad,
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                label = "Médias",
                                count = stats.yellowCount,
                                pct = stats.yellowPct,
                                containerColor = YellowMedium,
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                label = "Boas",
                                count = stats.greenCount,
                                pct = stats.greenPct,
                                containerColor = GreenGood,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // ── Card de meta ────────────────────────────────────────────────────────
        GoalCard(
            goal = goal,
            progress = goalProgress,
            onClick = { showGoalDialog = true }
        )
    }
    }
}

@Composable
private fun StatsCard(
    label: String,
    count: Int,
    pct: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(18.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.30f), spotColor = Color.Black.copy(alpha = 0.22f))
            .border(1.5.dp, containerColor, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = PureWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$count",
                color = WarmOnBg,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = containerColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$pct%",
                color = WarmOnBg.copy(alpha = 0.45f),
                fontSize = 12.sp
            )
        }
    }
}
