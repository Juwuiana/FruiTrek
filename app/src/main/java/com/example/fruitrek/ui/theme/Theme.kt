package com.example.fruitrek.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FruiTrekColorScheme = lightColorScheme(
    primary        = TropicalGreen,
    onPrimary      = Color.White,
    secondary      = MangoOrange,
    onSecondary    = Color.White,
    tertiary       = SkyBlue,
    background     = CreamWhite,
    onBackground   = DarkJungle,
    surface        = Color.White,
    onSurface      = DarkJungle,
    surfaceVariant = SoftMint,
    error          = Color(0xFFB71C1C)
)

@Composable
fun FruiTrekTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FruiTrekColorScheme,
        typography  = Typography,
        content     = content
    )
}
