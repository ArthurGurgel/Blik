package com.example.blik

import android.content.Context

object DadosLocaisUsuario {

    private const val PREFS =
        "blik_dados_locais"

    private const val CHAVE_USUARIO =
        "usuario_vinculado"

    fun usuarioVinculado(
        context: Context
    ): String? {

        return context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .getString(
                CHAVE_USUARIO,
                null
            )
    }

    fun vincularOuValidar(
        context: Context,
        usuarioId: String
    ): Boolean {

        val prefs =
            context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )

        val usuarioExistente =
            prefs.getString(
                CHAVE_USUARIO,
                null
            )

        if (usuarioExistente == null) {

            prefs.edit()
                .putString(
                    CHAVE_USUARIO,
                    usuarioId
                )
                .apply()

            return true
        }

        return usuarioExistente ==
                usuarioId
    }
}