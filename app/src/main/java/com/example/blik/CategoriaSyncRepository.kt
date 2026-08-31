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

        if (categoriaDao.quantidade() > 0) {
            return 0
        }

        val categoriasRemotas =
            CategoriaRemotaRepository.listar()

        val categoriaOutrosRemota =
            categoriasRemotas
                .firstOrNull { categoria ->
                    categoria.id ==
                            "81d1679a-816a-4050-bfc0-d147731c1eba"
                }

        if (categoriaOutrosRemota == null) {
            throw IllegalStateException(
                "OUTROS NAO VEIO DO SUPABASE. TOTAL: ${categoriasRemotas.size}"
            )
        }

        if (categoriasRemotas.isEmpty()) {
            return 0
        }

        val categoriasLocais =
            categoriasRemotas.map { categoriaRemota ->

                CategoriaEntity(
                    nome = categoriaRemota.nome,
                    syncId = categoriaRemota.id
                )
            }

        categoriaDao.inserirTodas(
            categoriasLocais
        )

        val categoriaOutrosLocal =
            categoriaDao
                .listarTodasUmaVez()
                .firstOrNull { categoria ->
                    categoria.syncId ==
                            "81d1679a-816a-4050-bfc0-d147731c1eba"
                }

        if (categoriaOutrosLocal == null) {
            throw IllegalStateException(
                "OUTROS VEIO DO SUPABASE MAS NAO ENTROU NO ROOM"
            )
        }

        return categoriasLocais.size
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