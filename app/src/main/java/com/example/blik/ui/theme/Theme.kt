package com.example.blik.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.blik.ModoTema


private val BlikLightColorScheme =
    lightColorScheme(

        primary =
            BlikPrimary,

        onPrimary =
            BlikOnPrimary,

        primaryContainer =
            BlikPrimaryContainer,

        onPrimaryContainer =
            BlikOnPrimaryContainer,


        secondary =
            BlikPrimaryDark,

        onSecondary =
            BlikOnPrimary,

        secondaryContainer =
            BlikPrimaryContainer,

        onSecondaryContainer =
            BlikOnPrimaryContainer,


        tertiary =
            BlikFatura,

        onTertiary =
            BlikOnPrimary,

        tertiaryContainer =
            BlikFaturaContainer,

        onTertiaryContainer =
            BlikFatura,


        background =
            BlikBackground,

        onBackground =
            BlikTextPrimary,


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


        outline =
            BlikOutline,

        outlineVariant =
            BlikOutline,


        error =
            BlikError,

        onError =
            BlikOnPrimary,

        errorContainer =
            BlikSaidaContainer,

        onErrorContainer =
            BlikSaida
    )


private val BlikDarkColorScheme =
    darkColorScheme(

        primary =
            BlikPrimaryDarkTheme,

        onPrimary =
            BlikOnPrimaryDarkTheme,

        primaryContainer =
            BlikPrimaryContainerDarkTheme,

        onPrimaryContainer =
            BlikOnPrimaryContainerDarkTheme,


        secondary =
            BlikPrimaryDarkTheme,

        onSecondary =
            BlikOnPrimaryDarkTheme,

        secondaryContainer =
            BlikHomeCardDark,

        onSecondaryContainer =
            BlikHomeCardTextDark,


        tertiary =
            BlikFaturaDarkTheme,

        onTertiary =
            BlikBackgroundDarkTheme,

        tertiaryContainer =
            BlikFaturaContainerDarkTheme,

        onTertiaryContainer =
            BlikFaturaDarkTheme,


        background =
            BlikBackgroundDarkTheme,

        onBackground =
            BlikTextPrimaryDarkTheme,


        surface =
            BlikSurfaceDarkTheme,

        onSurface =
            BlikTextPrimaryDarkTheme,

        surfaceVariant =
            BlikSurfaceVariantDarkTheme,

        onSurfaceVariant =
            BlikTextSecondaryDarkTheme,


        surfaceContainerLowest =
            BlikBackgroundDarkTheme,

        surfaceContainerLow =
            BlikSurfaceDarkTheme,

        surfaceContainer =
            BlikSurfaceDarkTheme,

        surfaceContainerHigh =
            BlikSurfaceVariantDarkTheme,

        surfaceContainerHighest =
            BlikSurfaceVariantDarkTheme,


        outline =
            BlikOutlineDarkTheme,

        outlineVariant =
            BlikOutlineDarkTheme,


        error =
            BlikErrorDarkTheme,

        onError =
            BlikBackgroundDarkTheme,

        errorContainer =
            BlikErrorContainerDarkTheme,

        onErrorContainer =
            BlikErrorDarkTheme
    )


@Composable
fun BlikTheme(
    modoTema: ModoTema =
        ModoTema.SISTEMA,

    content: @Composable () -> Unit
) {

    val usarTemaEscuro =
        when (modoTema) {

            ModoTema.SISTEMA ->
                isSystemInDarkTheme()

            ModoTema.CLARO ->
                false

            ModoTema.ESCURO ->
                true
        }


    MaterialTheme(
        colorScheme =
            if (usarTemaEscuro) {
                BlikDarkColorScheme
            } else {
                BlikLightColorScheme
            },

        typography =
            Typography,

        content =
            content
    )
}