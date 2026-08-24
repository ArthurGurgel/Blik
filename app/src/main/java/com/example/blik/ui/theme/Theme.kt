package com.example.blik.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val BlikColorScheme =
    lightColorScheme(

        // =================================================
        // PRINCIPAL
        // =================================================

        primary = BlikPrimary,
        onPrimary = BlikOnPrimary,

        primaryContainer =
            BlikPrimaryContainer,

        onPrimaryContainer =
            BlikOnPrimaryContainer,


        // =================================================
        // SECUNDÁRIA
        // =================================================

        secondary =
            BlikPrimaryDark,

        onSecondary =
            BlikOnPrimary,

        secondaryContainer =
            BlikPrimaryContainer,

        onSecondaryContainer =
            BlikOnPrimaryContainer,


        // =================================================
        // TERCIÁRIA
        // =================================================

        tertiary =
            BlikPrimaryDark,

        onTertiary =
            BlikOnPrimary,

        tertiaryContainer =
            BlikPrimaryContainer,

        onTertiaryContainer =
            BlikOnPrimaryContainer,


        // =================================================
        // FUNDO
        // =================================================

        background =
            BlikBackground,

        onBackground =
            BlikTextPrimary,


        // =================================================
        // SUPERFÍCIES / CARDS
        // =================================================

        surface =
            BlikSurface,

        onSurface =
            BlikTextPrimary,

        surfaceVariant =
            BlikSurfaceVariant,

        onSurfaceVariant =
            BlikTextSecondary,


        surfaceContainerLowest =
            BlikSurface,

        surfaceContainerLow =
            BlikSurface,

        surfaceContainer =
            BlikSurface,

        surfaceContainerHigh =
            BlikSurfaceVariant,

        surfaceContainerHighest =
            BlikSurfaceVariant,


        // =================================================
        // BORDAS
        // =================================================

        outline =
            BlikOutline,

        outlineVariant =
            BlikOutline,


        // =================================================
        // ERRO
        // =================================================

        error =
            BlikError,

        onError =
            BlikOnPrimary,

        errorContainer =
            BlikSaidaContainer,

        onErrorContainer =
            BlikSaida
    )


@Composable
fun BlikTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme =
            BlikColorScheme,

        typography =
            Typography,

        content =
            content
    )
}