package com.example.blik

import io.github.jan.supabase.postgrest.from

object CategoriaRemotaRepository {

    suspend fun inserir(
        categoria: CategoriaRemotaNova
    ): CategoriaRemota {

        return SupabaseProvider.client
            .from("categorias")
            .insert(categoria) {
                select()
            }
            .decodeSingle<CategoriaRemota>()
    }


    suspend fun listar(): List<CategoriaRemota> {

        return SupabaseProvider.client
            .from("categorias")
            .select()
            .decodeList<CategoriaRemota>()
    }
}