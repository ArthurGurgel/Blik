package com.example.blik

import io.github.jan.supabase.postgrest.from


object OrcamentoRemotoRepository {


    suspend fun listar():
            List<OrcamentoRemoto> {

        return SupabaseProvider.client
            .from("orcamentos")
            .select()
            .decodeList<OrcamentoRemoto>()
    }


    suspend fun sincronizar(
        orcamento: OrcamentoRemotoNovo
    ) {

        SupabaseProvider.client
            .from("orcamentos")
            .upsert(
                orcamento
            )
    }
}