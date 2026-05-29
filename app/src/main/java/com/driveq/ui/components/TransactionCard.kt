package com.driveq.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveq.model.Transaction
import com.driveq.model.TransactionType
import com.driveq.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) WarmYellow else RedFinance
    val prefix = if (isIncome) "+" else "-"
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.42f), spotColor = Color.Black.copy(alpha = 0.30f))
            .border(0.5.dp, WarmOutline.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Info ────────────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category.label,
                    color = WarmOnBg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        color = WarmOnSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(transaction.date)),
                    color = WarmPlaceholder,
                    fontSize = 11.sp
                )
            }

            // ── Valor ───────────────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isIncome) LightYellowContainer else RedFinance.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "$prefix R$ %.2f".format(transaction.amount),
                    color = amountColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // ── Delete ──────────────────────────────────────────────────────────
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Excluir",
                    tint = RedFinance.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
