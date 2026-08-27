package com.example.blik

import io.github.jan.supabase.postgrest.from

object ContaRemotaRepository {

    suspend fun inserir(
        conta: ContaRemotaNova
    ): ContaRemota {

        return SupabaseProvider.client
            .from("contas")
            .insert(conta) {
                select()
            }
            .decodeSingle<ContaRemota>()
    }

    suspend fun listar(): List<ContaRemota> {

        return SupabaseProvider.client
            .from("contas")
            .select()
            .decodeList<ContaRemota>()
    }

    suspend fun sincronizar(
        conta: ContaRemotaNova
    ) {
        SupabaseProvider.client
            .from("contas")
            .upsert(conta)
    }
}