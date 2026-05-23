package com.uberfilter.ui.screens.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uberfilter.model.TransactionCategory
import com.uberfilter.model.TransactionType
import com.uberfilter.ui.FinanceViewModel
import com.uberfilter.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    vm: FinanceViewModel,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = remember(selectedType) {
        when (selectedType) {
            TransactionType.INCOME -> TransactionCategory.incomeCategories()
            TransactionType.EXPENSE -> TransactionCategory.expenseCategories()
        }
    }

    LaunchedEffect(selectedType) {
        selectedCategory = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ── Cabeçalho ───────────────────────────────────────────────────────────
        Text(
            text = "Nova Transação",
            color = WarmYellow,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        // ── Tipo (Receita / Despesa) ────────────────────────────────────────────
        Text(
            text = "TIPO",
            color = WarmOnSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                TransactionType.INCOME to "Receita",
                TransactionType.EXPENSE to "Despesa"
            ).forEach { (type, label) ->
                val isSelected = selectedType == type
                val chipColor = when (type) {
                    TransactionType.INCOME -> WarmYellow
                    TransactionType.EXPENSE -> RedFinance
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedType = type },
                    shape = RoundedCornerShape(14.dp),
                    label = {
                        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chipColor,
                        selectedLabelColor = WarmOnBg,
                        containerColor = PureWhite,
                        labelColor = WarmOnSurfaceVariant
                    )
                )
            }
        }

        // ── Categoria ───────────────────────────────────────────────────────────
        Text(
            text = "CATEGORIA",
            color = WarmOnSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                Surface(
                    onClick = { selectedCategory = category },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) LightYellowContainer else PureWhite
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSelected) "●" else "○",
                            color = if (isSelected) WarmYellow else WarmPlaceholder,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = category.label,
                            color = if (isSelected) WarmOnBg else WarmOnSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // ── Valor ───────────────────────────────────────────────────────────────
        Text(
            text = "VALOR (R$)",
            color = WarmOnSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it; errorMessage = null },
            placeholder = { Text("0,00", color = WarmPlaceholder) },
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
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite
            )
        )

        // ── Descrição ───────────────────────────────────────────────────────────
        Text(
            text = "DESCRIÇÃO (OPCIONAL)",
            color = WarmOnSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Ex: Combustível Posto Ipiranga", color = WarmPlaceholder) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
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

        // ── Erro ────────────────────────────────────────────────────────────────
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = RedFinance,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // ── Botão salvar ────────────────────────────────────────────────────────
        Button(
            onClick = {
                val value = amount.replace(',', '.').toDoubleOrNull()
                when {
                    value == null || value <= 0 -> errorMessage = "Informe um valor válido maior que zero"
                    selectedCategory == null -> errorMessage = "Selecione uma categoria"
                    else -> {
                        vm.addTransaction(
                            type = selectedType,
                            category = selectedCategory!!,
                            amount = value,
                            description = description.trim(),
                            date = Calendar.getInstance().timeInMillis
                        )
                        onBack()
                    }
                }
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
                text = "Salvar Transação",
                color = OnWarmYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
