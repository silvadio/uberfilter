package com.driveq.ui.screens.finance

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveq.ui.FinanceViewModel
import com.driveq.ui.components.*
import com.driveq.ui.theme.*

@Composable
fun FinanceScreen(
    vm: FinanceViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    val uiState by vm.uiState.collectAsState()
    val selectedPeriod by vm.selectedPeriod.collectAsState()
    val goal by vm.goal.collectAsState()
    val goalProgress by vm.goalProgress.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }

    if (showGoalDialog) {
        GoalDialog(
            currentGoal = goal,
            onDismiss = { showGoalDialog = false },
            onSave = { type, amount -> vm.setGoal(type, amount) },
            onClear = { vm.clearGoal() }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ── Título ──────────────────────────────────────────────────────────────
        item {
            Text(
                text = "Finanças",
                color = WarmYellow,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // ── Card de meta ────────────────────────────────────────────────────────
        item {
            GoalCard(
                goal = goal,
                progress = goalProgress,
                onClick = { showGoalDialog = true }
            )
        }

        // ── Links de histórico ──────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onNavigateToHistory) {
                    Text(
                        text = "Metas  →",
                        color = WarmYellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onNavigateToTransactions) {
                    Text(
                        text = "Transações  →",
                        color = WarmYellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ── Resumo ──────────────────────────────────────────────────────────────
        if (uiState.summary != null) {
            item {
                SummaryCard(
                    summary = uiState.summary!!,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelect = { vm.setPeriod(it) }
                )
            }
        }

        // ── Transações ──────────────────────────────────────────────────────────
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WarmYellow, strokeWidth = 3.dp)
                }
            }
        } else if (uiState.transactions.isEmpty()) {
            item { EmptyState() }
        } else {
            item {
                Text(
                    text = "TRANSAÇÕES DO DIA",
                    color = WarmOnSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }

            items(
                items = uiState.transactions,
                key = { it.id }
            ) { transaction ->
                TransactionCard(
                    transaction = transaction,
                    onDelete = { vm.deleteTransaction(transaction) }
                )
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun EmptyState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.42f), spotColor = Color.Black.copy(alpha = 0.30f))
            .border(0.5.dp, WarmOutline.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = WarmYellow.copy(alpha = 0.5f),
                modifier = Modifier.size(52.dp)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Nenhuma transação no período",
                color = WarmOnSurfaceVariant,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Toque no + para adicionar",
                color = WarmPlaceholder,
                fontSize = 13.sp
            )
        }
    }
}
