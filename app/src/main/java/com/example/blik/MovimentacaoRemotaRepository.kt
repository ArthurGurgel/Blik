package com.example.blik

import io.github.jan.supabase.postgrest.from

object MovimentacaoRemotaRepository {

    suspend fun inserir(
        movimentacao: MovimentacaoRemotaNova
    ): MovimentacaoRemota {

        return SupabaseProvider.client
            .from("movimentacoes")
            .insert(movimentacao) {
                select()
            }
            .decodeSingle<MovimentacaoRemota>()
    }


    suspend fun listar(): List<MovimentacaoRemota> {

        return SupabaseProvider.client
            .from("movimentacoes")
            .select()
            .decodeList<MovimentacaoRemota>()
    }

    suspend fun sincronizar(
        movimentacao: MovimentacaoRemotaNova
    ) {
        SupabaseProvider.client
            .from("movimentacoes")
            .upsert(movimentacao)
    }
}