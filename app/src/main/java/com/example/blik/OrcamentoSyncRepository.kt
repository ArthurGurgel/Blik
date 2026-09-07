package com.example.blik

import io.github.jan.supabase.postgrest.from


object OrcamentoSyncRepository {


    suspend fun sincronizar(
        orcamento: OrcamentoEntity,
        categorias: List<CategoriaEntity>,
        usuarioId: String
    ) {

        val syncId =
            orcamento.syncId
                ?: throw IllegalStateException(
                    "O orçamento não possui syncId."
                )


        val categoriaSyncId =
            if (
                orcamento.tipo ==
                "CATEGORIA"
            ) {

                val categoria =
                    categorias
                        .firstOrNull {
                            it.id ==
                                    orcamento.categoriaId
                        }
                        ?: throw IllegalStateException(
                            "A categoria vinculada ao orçamento não foi encontrada."
                        )


                categoria.syncId
                    ?: throw IllegalStateException(
                        "A categoria \"${categoria.nome}\" não possui syncId."
                    )

            } else {

                null
            }


        OrcamentoRemotoRepository
            .sincronizar(

                OrcamentoRemotoNovo(

                    id =
                        syncId,

                    userId =
                        usuarioId,

                    referencia =
                        orcamento.referencia,

                    mes =
                        orcamento.mes,

                    ano =
                        orcamento.ano,

                    tipo =
                        orcamento.tipo,

                    categoriaId =
                        categoriaSyncId,

                    limiteMensal =
                        orcamento.limiteMensal,

                    ativo =
                        orcamento.ativo
                )
            )
    }


    suspend fun sincronizarTodos(
        orcamentos: List<OrcamentoEntity>,
        categorias: List<CategoriaEntity>,
        usuarioId: String
    ) {

        orcamentos.forEach { orcamento ->

            sincronizar(
                orcamento =
                    orcamento,

                categorias =
                    categorias,

                usuarioId =
                    usuarioId
            )
        }
    }


    suspend fun baixarTodosParaRoom(
        orcamentoDao: OrcamentoDao,
        categoriaDao: CategoriaDao
    ): Int {

        val orcamentosRemotos =
            OrcamentoRemotoRepository.listar()


        if (
            orcamentosRemotos.isEmpty()
        ) {
            return 0
        }


        val categoriasLocais =
            categoriaDao
                .listarTodasUmaVez()


        var quantidadeAlterada =
            0


        orcamentosRemotos
            .forEach { remoto ->


                val categoriaLocal =
                    if (
                        remoto.categoriaId != null
                    ) {

                        categoriasLocais
                            .firstOrNull {
                                it.syncId ==
                                        remoto.categoriaId
                            }
                            ?: throw IllegalStateException(
                                "A categoria do orçamento " +
                                        "\"${remoto.referencia}\" " +
                                        "não foi encontrada neste aparelho."
                            )

                    } else {

                        null
                    }


                val local =
                    orcamentoDao
                        .buscarPorSyncId(
                            remoto.id
                        )


                if (
                    local == null
                ) {

                    val resultado =
                        orcamentoDao
                            .inserir(

                                OrcamentoEntity(

                                    referencia =
                                        remoto.referencia,

                                    mes =
                                        remoto.mes,

                                    ano =
                                        remoto.ano,

                                    tipo =
                                        remoto.tipo,

                                    categoriaId =
                                        categoriaLocal?.id,

                                    limiteMensal =
                                        remoto.limiteMensal,

                                    ativo =
                                        remoto.ativo,

                                    syncId =
                                        remoto.id
                                )
                            )


                    if (
                        resultado == -1L
                    ) {

                        throw IllegalStateException(
                            "Não foi possível importar o orçamento " +
                                    "\"${remoto.referencia}\"."
                        )
                    }


                    quantidadeAlterada++

                } else {

                    val precisaAtualizar =

                        local.referencia !=
                                remoto.referencia ||

                                local.mes !=
                                remoto.mes ||

                                local.ano !=
                                remoto.ano ||

                                local.tipo !=
                                remoto.tipo ||

                                local.categoriaId !=
                                categoriaLocal?.id ||

                                local.limiteMensal !=
                                remoto.limiteMensal ||

                                local.ativo !=
                                remoto.ativo


                    if (
                        precisaAtualizar
                    ) {

                        val alteradas =
                            orcamentoDao
                                .atualizarDaNuvem(

                                    syncId =
                                        remoto.id,

                                    referencia =
                                        remoto.referencia,

                                    mes =
                                        remoto.mes,

                                    ano =
                                        remoto.ano,

                                    tipo =
                                        remoto.tipo,

                                    categoriaId =
                                        categoriaLocal?.id,

                                    limiteMensal =
                                        remoto.limiteMensal,

                                    ativo =
                                        remoto.ativo
                                )


                        quantidadeAlterada +=
                            alteradas
                    }
                }
            }


        return quantidadeAlterada
    }


    suspend fun excluir(
        syncId: String
    ) {

        SupabaseProvider.client
            .from("orcamentos")
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