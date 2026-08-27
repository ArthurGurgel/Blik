package com.example.blik

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
}