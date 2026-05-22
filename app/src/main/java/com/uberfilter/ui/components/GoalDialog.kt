package com.uberfilter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uberfilter.model.Goal
import com.uberfilter.model.GoalType
import com.uberfilter.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDialog(
    currentGoal: Goal,
    onDismiss: () -> Unit,
    onSave: (type: GoalType, amount: Double) -> Unit,
    onClear: () -> Unit
) {
    var selectedType by remember { mutableStateOf(currentGoal.type) }
    var amountText by remember {
        mutableStateOf(
            if (currentGoal.targetAmount > 0) "%.2f".format(currentGoal.targetAmount) else ""
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        titleContentColor = WarmYellow,
        textContentColor = WarmOnBg,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Meta Financeira",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // ── Tipo ────────────────────────────────────────────────────────
                Text(
                    text = "TIPO",
                    color = WarmOnSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )

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
                                containerColor = LightYellowBg,
                                labelColor = WarmOnSurfaceVariant
                            )
                        )
                    }
                }

                // ── Valor ───────────────────────────────────────────────────────
                Text(
                    text = "VALOR DA META (R$)",
                    color = WarmOnSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; error = null },
                    placeholder = { Text("0,00", color = WarmPlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                // ── Erro ────────────────────────────────────────────────────────
                if (error != null) {
                    Text(text = error!!, color = RedFinance, fontSize = 12.sp)
                }

                // ── Remover meta ────────────────────────────────────────────────
                if (currentGoal.targetAmount > 0) {
                    TextButton(
                        onClick = {
                            onClear()
                            onDismiss()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Remover meta",
                            color = WarmPlaceholder,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = amountText.replace(',', '.').toDoubleOrNull()
                    when {
                        value == null || value <= 0 -> error = "Informe um valor válido"
                        else -> {
                            onSave(selectedType, value)
                            onDismiss()
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmYellow,
                    contentColor = OnWarmYellow
                )
            ) {
                Text(
                    text = "Salvar Meta",
                    fontWeight = FontWeight.Bold,
                    color = OnWarmYellow
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = WarmOnSurfaceVariant)
            }
        }
    )
}
