package com.uberfilter.ui.theme

import androidx.compose.ui.graphics.Color

// ── Família 1: Warm Yellow ─ Destaque, botões, indicadores ativos ────────────
val WarmYellow        = Color(0xFFD4A017)   // Harvest Gold – tom encorpado com bom contraste sobre claro
val WarmYellowDim     = Color(0xFFB8860B)   // Dark Goldenrod – estado pressionado / variante
val WarmYellowGlow    = Color(0x33D4A017)   // brilho sutil (alpha baixo)
val OnWarmYellow      = Color(0xFF2D2000)   // texto sobre botões preenchidos (marrom bem escuro)

// ── Família 2: Light Yellow ─ Containers, superfícies destacadas ─────────────
val LightYellowContainer  = Color(0xFFFFF1B8)   // container primary
val OnLightYellowContainer = Color(0xFF3D2E00)   // texto sobre container
val LightYellowSurface    = Color(0xFFFFF3CD)   // amarelo palha – cards destacados, chips
val LightYellowBg         = Color(0xFFFFF9E6)   // marfim suave – seções com destaque sutil

// ── Família 3: Neutros Quentes ─ Fundo, texto, bordas ────────────────────────
val WarmWhite         = Color(0xFFFFFDF7)   // fundo principal (branco quente / floral white)
val PureWhite         = Color(0xFFFFFFFF)   // cards primários
val WarmOnBg          = Color(0xFF1E1B14)   // texto principal (quase preto com tom quente)
val WarmOnSurfaceVariant = Color(0xFF6B6556) // texto secundário (cinza quente)
val WarmPlaceholder   = Color(0xFF9B9586)   // placeholder / hint
val WarmOutline       = Color(0xFFE0D8C8)   // bordas (bege acinzentado)
val WarmOutlineVariant = Color(0xFFEDE5D5)  // bordas sutis / divisores leves

// ── Semânticas ──────────────────────────────────────────────────────────────
val GreenFinance     = Color(0xFF43A047)   // receita / saldo positivo (verde levemente oliva)
val RedFinance       = Color(0xFFD32F2F)   // despesa / saldo negativo (vermelho mais terroso)
val GreenGlow        = Color(0x3343A047)
val RedGlow          = Color(0x33D32F2F)

// ── Stats / Gamificação ─────────────────────────────────────────────────────
val RedBad           = Color(0xFFC62828)
val YellowMedium     = Color(0xFFF9A825)
val GreenGood        = Color(0xFF2E7D32)

// ── Transparentes para sobreposição ─────────────────────────────────────────
val BlackAlpha10     = Color(0x1A000000)
val BlackAlpha20     = Color(0x33000000)
