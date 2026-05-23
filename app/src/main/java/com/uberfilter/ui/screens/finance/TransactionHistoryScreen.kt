package com.uberfilter.ui.screens.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uberfilter.model.PeriodFilter
import com.uberfilter.model.TransactionType
import com.uberfilter.ui.FinanceViewModel
import com.uberfilter.ui.components.PeriodSelector
import com.uberfilter.ui.components.TransactionCard
import com.uberfilter.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    vm: FinanceViewModel,
    onBack: () -> Unit
) {
    val periodTransactions by vm.periodTransactions.collectAsState()
    val transactionPeriod by vm.selectedTransactionPeriod.collectAsState()

    var selectedType by remember { mutableStateOf<TransactionType?>(null) }

    val filtered = remember(periodTransactions, selectedType) {
        if (selectedType == null) periodTransactions
        else periodTransactions.filter { it.type == selectedType }
    }

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
                text = "Histórico de Transações",
                color = WarmYellow,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // ── Filtro de período ───────────────────────────────────────────────────
        PeriodSelector(
            selected = transactionPeriod,
            onSelect = { vm.setTransactionPeriod(it) }
        )

        // ── Filtros de tipo ─────────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = selectedType == null,
                onClick = { selectedType = null },
                shape = RoundedCornerShape(12.dp),
                label = {
                    Text(
                        text = "Todas",
                        fontSize = 14.sp,
                        fontWeight = if (selectedType == null) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WarmYellow,
                    selectedLabelColor = OnWarmYellow,
                    containerColor = PureWhite,
                    labelColor = WarmOnSurfaceVariant
                )
            )

            FilterChip(
                selected = selectedType == TransactionType.INCOME,
                onClick = { selectedType = TransactionType.INCOME },
                shape = RoundedCornerShape(12.dp),
                label = {
                    Text(
                        text = "Receitas",
                        fontSize = 14.sp,
                        fontWeight = if (selectedType == TransactionType.INCOME) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WarmYellow,
                    selectedLabelColor = OnWarmYellow,
                    containerColor = PureWhite,
                    labelColor = WarmOnSurfaceVariant
                )
            )

            FilterChip(
                selected = selectedType == TransactionType.EXPENSE,
                onClick = { selectedType = TransactionType.EXPENSE },
                shape = RoundedCornerShape(12.dp),
                label = {
                    Text(
                        text = "Despesas",
                        fontSize = 14.sp,
                        fontWeight = if (selectedType == TransactionType.EXPENSE) FontWeight.Bold else FontWeight.Normal
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

        // ── Lista ───────────────────────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = WarmPlaceholder,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Nenhuma transação encontrada",
                        color = WarmOnSurfaceVariant,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Tente outro filtro ou adicione pelo botão +",
                        color = WarmPlaceholder,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered, key = { it.id }) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        onDelete = { vm.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }
}
