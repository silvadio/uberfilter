package com.driveq.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.driveq.ui.theme.PureWhite
import com.driveq.ui.theme.WarmOutline

/**
 * Card frame padrão para itens de listagem no app.
 *
 * Características:
 * - Cantos arredondados 16.dp
 * - Sombra elevada com 8.dp
 * - Borda sutil WarmOutline 0.5.dp
 * - Fundo PureWhite
 *
 * Usado em TransactionCard (Finanças), GeofenceItem e TextLocationItem (Destinos).
 */
@Composable
fun BaseListCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.42f),
                spotColor = Color.Black.copy(alpha = 0.30f)
            )
            .border(
                width = 0.5.dp,
                color = WarmOutline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        content()
    }
}
