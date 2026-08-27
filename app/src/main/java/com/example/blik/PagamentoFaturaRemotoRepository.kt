package com.example.blik

import io.github.jan.supabase.postgrest.from

object PagamentoFaturaRemotoRepository {

    suspend fun inserir(
        pagamento: PagamentoFaturaRemotoNovo
    ): PagamentoFaturaRemoto {

        return SupabaseProvider.client
            .from("pagamentos_fatura")
            .insert(pagamento) {
                select()
            }
            .decodeSingle<PagamentoFaturaRemoto>()
    }


    suspend fun listar(): List<PagamentoFaturaRemoto> {

        return SupabaseProvider.client
            .from("pagamentos_fatura")
            .select()
            .decodeList<PagamentoFaturaRemoto>()
    }
}