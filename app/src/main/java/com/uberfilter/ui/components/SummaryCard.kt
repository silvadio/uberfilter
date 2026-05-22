package com.uberfilter.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uberfilter.model.FinancialSummary
import com.uberfilter.model.PeriodFilter
import com.uberfilter.ui.theme.*

@Composable
fun SummaryCard(
    summary: FinancialSummary,
    selectedPeriod: PeriodFilter,
    onPeriodSelect: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.48f), spotColor = Color.Black.copy(alpha = 0.35f))
            .border(0.5.dp, WarmOutline.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // ── Cabeçalho com seletor de período inline ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PeriodFilter.entries.forEach { filter ->
                    val isSelected = filter == selectedPeriod
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPeriodSelect(filter) },
                        shape = RoundedCornerShape(10.dp),
                        label = {
                            Text(
                                text = filter.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarmYellow,
                            selectedLabelColor = OnWarmYellow,
                            containerColor = LightYellowBg,
                            labelColor = WarmOnSurfaceVariant
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Saldo destacado ─────────────────────────────────────────────────
            val balanceColor = if (summary.balance >= 0) WarmYellow else RedFinance
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = balanceColor.copy(alpha = 0.10f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "R$ %.2f".format(summary.balance),
                        color = balanceColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Saldo",
                        color = WarmOnSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Linha Receita / Despesa / Registros ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    value = "R$ %.2f".format(summary.totalIncome),
                    label = "Receitas",
                    color = WarmYellow
                )
                MetricItem(
                    value = "R$ %.2f".format(summary.totalExpense),
                    label = "Despesas",
                    color = RedFinance
                )
                MetricItem(
                    value = "${summary.transactionCount}",
                    label = "Registros",
                    color = WarmOnBg
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = WarmOnSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
