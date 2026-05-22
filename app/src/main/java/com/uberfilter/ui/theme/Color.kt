package com.uberfilter.ui.theme

import androidx.compose.ui.graphics.Color

// ── Família 1: Black Accent — Destaque, botões, indicadores ativos ─────────────
val WarmYellow        = Color(0xFF1A1A1A)   // Off-black – tom elegante com bom contraste sobre claro
val WarmYellowDim     = Color(0xFF000000)   // Preto puro – estado pressionado / variante
val WarmYellowGlow    = Color(0x1A000000)   // sombra sutil (preto translúcido)
val OnWarmYellow      = Color(0xFFFFFFFF)   // texto sobre botões preenchidos (branco)

// ── Família 2: Light Gray — Containers, superfícies destacadas ───────────────
val LightYellowContainer  = Color(0xFFF5F5F5)   // container primary (cinza quase branco)
val OnLightYellowContainer = Color(0xFF1A1A1A)   // texto sobre container (preto)
val LightYellowSurface    = Color(0xFFEEEEEE)   // cards destacados, chips
val LightYellowBg         = Color(0xFFFAFAFA)   // off-white – seções com destaque sutil

// ── Família 3: Light Theme — Fundo, texto, bordas ────────────────────────────
val WarmWhite         = Color(0xFFFFFFFF)   // fundo principal (branco puro)
val PureWhite         = Color(0xFFFFFFFF)   // cards primários
val WarmOnBg          = Color(0xFF1A1A1A)   // texto principal (preto)
val WarmOnSurfaceVariant = Color(0xFF6E6E6E) // texto secundário (cinza médio)
val WarmPlaceholder   = Color(0xFF9E9E9E)   // placeholder / hint
val WarmOutline       = Color(0xFFE0E0E0)   // bordas (cinza claro)
val WarmOutlineVariant = Color(0xFFEEEEEE)  // bordas sutis / divisores

// ── Semânticas ──────────────────────────────────────────────────────────────
val GreenFinance     = Color(0xFF1A1A1A)   // receita / saldo positivo (preto – elegante)
val RedFinance       = Color(0xFFD32F2F)   // despesa / saldo negativo (mantido)
val GreenGlow        = Color(0x1A000000)
val RedGlow          = Color(0x33D32F2F)

// ── Stats / Gamificação ─────────────────────────────────────────────────────
val RedBad           = Color(0xFFC62828)
val YellowMedium     = Color(0xFFF9A825)
val GreenGood        = Color(0xFF1A1A1A)   // preto no lugar do verde

// ── Transparentes para sobreposição (sobre fundo claro) ─────────────────────
val BlackAlpha10     = Color(0x1A000000)
val BlackAlpha20     = Color(0x33000000)
