package com.example.blik

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.blikDataStore by
preferencesDataStore(
    name = "blik_preferencias"
)


enum class ModoTema {
    SISTEMA,
    CLARO,
    ESCURO
}


class PreferenciasAppRepository(
    context: Context
) {

    private val appContext =
        context.applicationContext


    companion object {

        private val CHAVE_TEMA =
            stringPreferencesKey(
                "modo_tema"
            )
    }


    val modoTema: Flow<ModoTema> =
        appContext
            .blikDataStore
            .data
            .map { preferencias ->

                when (
                    preferencias[
                        CHAVE_TEMA
                    ]
                ) {

                    ModoTema.CLARO.name ->
                        ModoTema.CLARO

                    ModoTema.ESCURO.name ->
                        ModoTema.ESCURO

                    else ->
                        ModoTema.SISTEMA
                }
            }


    suspend fun salvarModoTema(
        modoTema: ModoTema
    ) {

        appContext
            .blikDataStore
            .edit { preferencias ->

                preferencias[
                    CHAVE_TEMA
                ] =
                    modoTema.name
            }
    }
}