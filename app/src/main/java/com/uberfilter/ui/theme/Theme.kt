package com.uberfilter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PremiumLightColors = lightColorScheme(
    primary            = WarmYellow,
    onPrimary          = OnWarmYellow,
    primaryContainer   = LightYellowContainer,
    onPrimaryContainer = OnLightYellowContainer,
    secondary          = LightYellowSurface,
    onSecondary        = WarmOnBg,
    tertiary           = WarmOutlineVariant,
    onTertiary         = WarmOnSurfaceVariant,
    background         = WarmWhite,
    onBackground       = WarmOnBg,
    surface            = PureWhite,
    onSurface          = WarmOnBg,
    surfaceVariant     = LightYellowBg,
    onSurfaceVariant   = WarmOnSurfaceVariant,
    outline            = WarmOutline,
    outlineVariant     = WarmOutlineVariant,
    error              = RedFinance,
    onError            = PureWhite,
    scrim              = Color.Black.copy(alpha = 0.5f)
)

private val PremiumShapes = Shapes(
    small  = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large  = RoundedCornerShape(24.dp)
)

@Composable
fun UberFilterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PremiumLightColors,
        shapes      = PremiumShapes,
        typography  = Typography,
        content     = content
    )
}
