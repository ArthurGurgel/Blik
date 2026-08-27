package com.example.blik

import io.github.jan.supabase.postgrest.from

object ContaRemotaRepository {

    suspend fun inserir(
        conta: ContaRemotaNova
    ) {

        SupabaseProvider.client
            .from("contas")
            .insert(conta)
    }
}