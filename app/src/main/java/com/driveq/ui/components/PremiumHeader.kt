package com.driveq.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driveq.ui.theme.*

// ── PremiumHeader ─────────────────────────────────────────────────────────────

@Composable
fun PremiumHeader(
    greetingName: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    avatarPainter: Painter? = null,
    avatarInitials: String = "",
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(LightYellowBg)
            .clip(RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Lado Esquerdo: Avatar + Saudação ─────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar circular
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            ambientColor = WarmYellow,
                            spotColor = WarmYellow
                        )
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = OnWarmYellow.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .background(WarmYellow)
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarContent(
                        painter = avatarPainter,
                        initials = avatarInitials
                    )
                }

                // Saudação + Subtítulo
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Olá, $greetingName",
                        color = WarmOnBg,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp,
                        letterSpacing = (-0.5).sp
                    )

                    Text(
                        text = subtitle,
                        color = WarmYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        lineHeight = 18.sp,
                        letterSpacing = 0.15.sp
                    )
                }
            }

            // ── Lado Direito: Settings + Logout ─────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Configurações",
                        tint = WarmOnBg,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onLogoutClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Sair",
                        tint = WarmOnBg,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// ── Conteúdo do avatar: imagem, iniciais ou placeholder ───────────────────────

@Composable
private fun AvatarContent(
    painter: Painter?,
    initials: String
) {
    when {
        painter != null -> {
            Image(
                painter = painter,
                contentDescription = "Avatar do usuário",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
        initials.isNotEmpty() -> {
            Text(
                text = initials.take(2).uppercase(),
                color = OnWarmYellow,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Avatar",
                tint = OnWarmYellow.copy(alpha = 0.45f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ── Variante compacta para telas secundárias ──────────────────────────────────

@Composable
fun PremiumHeaderCompact(
    greetingName: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(LightYellowBg)
            .clip(RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Olá, $greetingName",
                    color = WarmOnBg,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = subtitle,
                    color = WarmYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 18.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Configurações",
                        tint = WarmOnBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onLogoutClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Sair",
                        tint = WarmOnBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
