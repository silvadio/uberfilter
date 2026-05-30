package com.driveq.ui.screens.finance

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveq.model.GoalHistoryEntry
import com.driveq.model.GoalType
import com.driveq.ui.FinanceViewModel
import com.driveq.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalHistoryScreen(
    vm: FinanceViewModel,
    onBack: () -> Unit
) {
    val history by vm.goalHistory.collectAsState()
    val goal by vm.goal.collectAsState()

    var selectedType by remember { mutableStateOf(goal.type) }

    val filteredHistory = remember(history, selectedType) {
        history.filter { it.goalType == selectedType }
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ── Topo com voltar ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = WarmYellow
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Histórico de Metas",
                color = WarmYellow,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // ── Toggle Semanal / Mensal ─────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GoalType.entries.forEach { type ->
                val isSelected = type == selectedType
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedType = type },
                    shape = RoundedCornerShape(12.dp),
                    label = {
                        Text(
                            text = type.label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarmYellow,
                        selectedLabelColor = OnWarmYellow,
                        containerColor = PureWhite,
                        labelColor = WarmOnSurfaceVariant
                    )
                )
            }
        }

        // ── Lista ───────────────────────────────────────────────────────────────
        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.EmojiEvents,
                        contentDescription = "Nenhuma meta concluída",
                        tint = WarmYellow.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Nenhuma meta concluída ainda",
                        color = WarmOnSurfaceVariant,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "O histórico aparece quando um período se encerra",
                        color = WarmPlaceholder,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredHistory) { entry ->
                    HistoryEntryCard(entry, dateFormat)
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(
    entry: GoalHistoryEntry,
    dateFormat: SimpleDateFormat
) {
    val periodLabel = remember(entry) {
        val start = dateFormat.format(Date(entry.periodStart))
        val end   = dateFormat.format(Date(entry.periodEnd))
        "de $start a $end"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.42f), spotColor = Color.Black.copy(alpha = 0.30f))
            .border(0.5.dp, WarmOutline.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Período + badge ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = periodLabel,
                    color = WarmOnSurfaceVariant,
                    fontSize = 12.sp
                )

                if (entry.wasAchieved) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarmYellow
                    ) {
                        Text(
                            text = "🏆 Batido!",
                            color = OnWarmYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Barra + valores ─────────────────────────────────────────────────
            val fraction = (entry.achievedPct.coerceAtMost(100)) / 100f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(4.dp),
                    color = LightYellowBg
                ) {}

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(4.dp),
                    color = if (entry.wasAchieved) WarmYellow else WarmYellow.copy(alpha = 0.6f)
                ) {}
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "R$ %.2f".format(entry.achievedBalance),
                    color = WarmOnBg,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${entry.achievedPct}%",
                    color = if (entry.wasAchieved) WarmYellow else WarmOnSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Meta: R$ %.2f".format(entry.targetAmount),
                color = WarmPlaceholder,
                fontSize = 11.sp
            )
        }
    }
}
