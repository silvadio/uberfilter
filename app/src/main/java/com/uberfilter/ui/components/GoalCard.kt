package com.uberfilter.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uberfilter.model.Goal
import com.uberfilter.model.GoalProgress
import com.uberfilter.model.GoalType
import com.uberfilter.ui.theme.*
import java.util.Calendar

@Composable
fun GoalCard(
    goal: Goal,
    progress: GoalProgress?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasGoal = goal.targetAmount > 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.48f), spotColor = Color.Black.copy(alpha = 0.35f))
            .border(0.5.dp, WarmOutline.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // ── Cabeçalho ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meta ${goal.type.label}",
                    color = WarmOnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ── Dias restantes ──────────────────────────────────────────
                    if (hasGoal) {
                        val daysLabel = daysRemainingLabel(goal.type)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarmYellowGlow
                        ) {
                            Text(
                                text = daysLabel,
                                color = WarmYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // ── Badge "Batido!" ─────────────────────────────────────────
                    if (progress?.isAchieved == true) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarmYellow
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.EmojiEvents,
                                    contentDescription = null,
                                    tint = OnWarmYellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Batido!",
                                    color = OnWarmYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (!hasGoal) {
                // ── Sem meta definida ───────────────────────────────────────────
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.TrackChanges,
                        contentDescription = null,
                        tint = WarmYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Definir meta",
                        color = WarmYellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                // ── Meta definida ───────────────────────────────────────────────
                Spacer(Modifier.height(12.dp))

                val collectedPct = progress?.percentage ?: 0
                val displayPct = if (progress != null && !progress.isAchieved) {
                    ((progress.remaining / goal.targetAmount) * 100).toInt().coerceIn(0, 100)
                } else {
                    collectedPct
                }
                val progressFraction = (displayPct.coerceAtMost(100)) / 100f
                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction,
                    animationSpec = tween(durationMillis = 600)
                )

                // Barra de progresso
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                ) {
                    // Fundo
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(7.dp),
                        color = LightYellowBg
                    ) {}

                    // Preenchimento
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(7.dp),
                        color = if (collectedPct >= 100) WarmYellow else WarmYellow
                    ) {}
                }

                Spacer(Modifier.height(12.dp))

                // Valores
                if (progress != null && !progress.isAchieved) {
                    // ── "Faltam" com destaque principal ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Faltam R$ %.2f".format(progress.remaining),
                            color = WarmYellow,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Percentagem (quanto falta)
                        Text(
                            text = "$displayPct%",
                            color = WarmOnBg,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    // ── Arrecadado + meta com menor destaque ──
                    Text(
                        text = "R$ %.2f de R$ %.2f".format(
                            progress.currentBalance,
                            goal.targetAmount
                        ),
                        color = WarmOnSurfaceVariant,
                        fontSize = 13.sp
                    )
                } else {
                    // ── Meta batida ou sem progresso ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "R$ %.2f".format(progress?.currentBalance ?: 0.0),
                                color = WarmOnBg,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "de R$ %.2f".format(goal.targetAmount),
                                color = WarmOnSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }

                        // Percentagem
                        Text(
                            text = "$displayPct%",
                            color = if (collectedPct >= 100) WarmYellow else WarmOnBg,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Helper: dias restantes no período da meta ────────────────────────────────

private fun daysRemainingLabel(type: GoalType): String {
    val now = Calendar.getInstance()
    val remaining = when (type) {
        GoalType.WEEKLY -> {
            // Próxima segunda-feira 04:00 (fim da semana Uber)
            val nextMon = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 4)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (!now.before(nextMon)) nextMon.add(Calendar.DAY_OF_YEAR, 7)
            val diffMs = nextMon.timeInMillis - now.timeInMillis
            ((diffMs + 86_399_999) / 86_400_000).toInt().coerceAtLeast(1)
        }
        GoalType.MONTHLY -> {
            val today = now.get(Calendar.DAY_OF_MONTH)
            val lastDay = now.getActualMaximum(Calendar.DAY_OF_MONTH)
            lastDay - today + 1
        }
    }
    return if (remaining == 1) "Último dia" else "$remaining dias"
}
