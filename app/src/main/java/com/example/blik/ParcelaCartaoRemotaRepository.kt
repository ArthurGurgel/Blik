package com.example.blik

import io.github.jan.supabase.postgrest.from

object ParcelaCartaoRemotaRepository {

    suspend fun inserir(
        parcela: ParcelaCartaoRemotaNova
    ): ParcelaCartaoRemota {

        return SupabaseProvider.client
            .from("parcelas_cartao")
            .insert(parcela) {
                select()
            }
            .decodeSingle<ParcelaCartaoRemota>()
    }


    suspend fun listar(): List<ParcelaCartaoRemota> {

        return SupabaseProvider.client
            .from("parcelas_cartao")
            .select()
            .decodeList<ParcelaCartaoRemota>()
    }
}