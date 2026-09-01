package com.example.blik

import io.github.jan.supabase.postgrest.from
object CategoriaSyncRepository {
    suspend fun sincronizar(
        categoria: CategoriaEntity,
        usuarioId: String
    ) {
        val syncId = categoria.syncId ?: throw IllegalStateException(
            "A categoria \"${categoria.nome}\" nao possui syncId."
        )

        CategoriaRemotaRepository.sincronizar(
            CategoriaRemotaNova(
                id = syncId,
                userId = usuarioId,
                nome = categoria.nome
            )
        )
    }

    suspend fun sincronizarTodas(
        categorias: List<CategoriaEntity>,
        usuarioId: String
    ) {
        categorias.forEach { categoria ->
            sincronizar(
                categoria = categoria,
                usuarioId = usuarioId
            )
        }
    }

    suspend fun baixarTodasParaRoom(
        categoriaDao: CategoriaDao
    ): Int {

        val categoriasRemotas =
            CategoriaRemotaRepository.listar()


        if (categoriasRemotas.isEmpty()) {
            return 0
        }


        var quantidadeAlterada = 0


        categoriasRemotas.forEach { categoriaRemota ->

            val categoriaLocal =
                categoriaDao.buscarPorSyncId(
                    syncId =
                        categoriaRemota.id
                )


            if (categoriaLocal == null) {

                // =============================================
                // EXISTE NA NUVEM, MAS NÃO NESTE APARELHO
                // =============================================

                val resultado =
                    categoriaDao.inserir(
                        CategoriaEntity(
                            nome =
                                categoriaRemota.nome,

                            syncId =
                                categoriaRemota.id
                        )
                    )


                if (resultado == -1L) {

                    throw IllegalStateException(
                        "Não foi possível importar a categoria \"${categoriaRemota.nome}\"."
                    )
                }


                quantidadeAlterada++

            } else {

                // =============================================
                // JÁ EXISTE LOCALMENTE.
                // VERIFICA SE O NOME MUDOU NA NUVEM
                // =============================================

                if (
                    categoriaLocal.nome !=
                    categoriaRemota.nome
                ) {

                    val linhasAtualizadas =
                        categoriaDao.atualizarDaNuvem(
                            syncId =
                                categoriaRemota.id,

                            nome =
                                categoriaRemota.nome
                        )


                    if (linhasAtualizadas > 0) {
                        quantidadeAlterada++
                    }
                }
            }
        }


        return quantidadeAlterada
    }

    suspend fun excluir(
        syncId: String
    ) {

        SupabaseProvider.client
            .from("categorias")
            .delete {

                filter {
                    eq(
                        "id",
                        syncId
                    )
                }
            }
    }
}