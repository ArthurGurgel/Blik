package com.example.blik

import io.github.jan.supabase.postgrest.from

object CartaoRemotoRepository {

    suspend fun inserir(
        cartao: CartaoRemotoNovo
    ): CartaoRemoto {

        return SupabaseProvider.client
            .from("cartoes")
            .insert(cartao) {
                select()
            }
            .decodeSingle<CartaoRemoto>()
    }


    suspend fun listar(): List<CartaoRemoto> {

        return SupabaseProvider.client
            .from("cartoes")
            .select()
            .decodeList<CartaoRemoto>()
    }
}