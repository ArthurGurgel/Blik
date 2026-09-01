package com.example.blik

import io.github.jan.supabase.postgrest.from

object ParcelaCartaoSyncRepository {

    // =============================================
    // ROOM -> SUPABASE
    // SINCRONIZA UMA PARCELA
    // =============================================

    suspend fun sincronizar(
        parcela: ParcelaCartaoEntity,
        movimentacoes: List<MovimentacaoEntity>,
        cartoes: List<CartaoEntity>,
        usuarioId: String
    ) {

        val syncId =
            parcela.syncId
                ?: throw IllegalStateException(
                    "A parcela ${parcela.numeroParcela} não possui syncId."
                )

        val movimentacao =
            movimentacoes
                .firstOrNull { movimentacao ->
                    movimentacao.id ==
                            parcela.movimentacaoId
                }
                ?: throw IllegalStateException(
                    "Movimentação da parcela " +
                            "${parcela.numeroParcela} não encontrada."
                )

        val movimentacaoSyncId =
            movimentacao.syncId
                ?: throw IllegalStateException(
                    "A movimentação da parcela " +
                            "${parcela.numeroParcela} não possui syncId."
                )

        val cartao =
            cartoes
                .firstOrNull { cartao ->
                    cartao.id ==
                            parcela.cartaoId
                }
                ?: throw IllegalStateException(
                    "Cartão da parcela " +
                            "${parcela.numeroParcela} não encontrado."
                )

        val cartaoSyncId =
            cartao.syncId
                ?: throw IllegalStateException(
                    "O cartão da parcela " +
                            "${parcela.numeroParcela} não possui syncId."
                )


        SupabaseProvider.client
            .from("parcelas_cartao")
            .upsert(
                ParcelaCartaoRemotaNova(
                    id =
                        syncId,

                    userId =
                        usuarioId,

                    movimentacaoId =
                        movimentacaoSyncId,

                    cartaoId =
                        cartaoSyncId,

                    numeroParcela =
                        parcela.numeroParcela,

                    totalParcelas =
                        parcela.totalParcelas,

                    valor =
                        parcela.valor,

                    mesFatura =
                        parcela.mesFatura,

                    anoFatura =
                        parcela.anoFatura,

                    quitadaAnteriormente =
                        parcela.quitadaAnteriormente
                )
            )
    }


    // =============================================
    // ROOM -> SUPABASE
    // SINCRONIZA TODAS
    // =============================================

    suspend fun sincronizarTodos(
        parcelas: List<ParcelaCartaoEntity>,
        movimentacoes: List<MovimentacaoEntity>,
        cartoes: List<CartaoEntity>,
        usuarioId: String
    ) {

        parcelas.forEach { parcela ->

            sincronizar(
                parcela = parcela,
                movimentacoes = movimentacoes,
                cartoes = cartoes,
                usuarioId = usuarioId
            )
        }
    }


    // =============================================
    // SUPABASE -> ROOM
    // DISPOSITIVO NOVO
    // =============================================

    suspend fun baixarTodasParaRoom(
        parcelaCartaoDao: ParcelaCartaoDao,
        movimentacaoDao: MovimentacaoDao,
        cartaoDao: CartaoDao
    ): Int {

        val parcelasRemotas =
            ParcelaCartaoRemotaRepository.listar()


        if (parcelasRemotas.isEmpty()) {
            return 0
        }


        val movimentacoesLocais =
            movimentacaoDao.listarTodasUmaVez()

        val cartoesLocais =
            cartaoDao.listarTodosUmaVez()


        var quantidadeAlterada = 0


        parcelasRemotas.forEach { parcelaRemota ->

            val movimentacaoLocal =
                movimentacoesLocais
                    .firstOrNull { movimentacao ->
                        movimentacao.syncId ==
                                parcelaRemota.movimentacaoId
                    }
                    ?: throw IllegalStateException(
                        "Movimentação da parcela " +
                                "${parcelaRemota.numeroParcela} " +
                                "não encontrada."
                    )


            val cartaoLocal =
                cartoesLocais
                    .firstOrNull { cartao ->
                        cartao.syncId ==
                                parcelaRemota.cartaoId
                    }
                    ?: throw IllegalStateException(
                        "Cartão da parcela " +
                                "${parcelaRemota.numeroParcela} " +
                                "não encontrado."
                    )


            val parcelaLocal =
                parcelaCartaoDao.buscarPorSyncId(
                    syncId =
                        parcelaRemota.id
                )


            if (parcelaLocal == null) {

                parcelaCartaoDao.inserir(
                    ParcelaCartaoEntity(
                        movimentacaoId =
                            movimentacaoLocal.id,

                        cartaoId =
                            cartaoLocal.id,

                        numeroParcela =
                            parcelaRemota.numeroParcela,

                        totalParcelas =
                            parcelaRemota.totalParcelas,

                        valor =
                            parcelaRemota.valor,

                        mesFatura =
                            parcelaRemota.mesFatura,

                        anoFatura =
                            parcelaRemota.anoFatura,

                        quitadaAnteriormente =
                            parcelaRemota.quitadaAnteriormente,

                        syncId =
                            parcelaRemota.id
                    )
                )


                quantidadeAlterada++

            } else {

                val precisaAtualizar =
                    parcelaLocal.movimentacaoId !=
                            movimentacaoLocal.id ||

                            parcelaLocal.cartaoId !=
                            cartaoLocal.id ||

                            parcelaLocal.numeroParcela !=
                            parcelaRemota.numeroParcela ||

                            parcelaLocal.totalParcelas !=
                            parcelaRemota.totalParcelas ||

                            parcelaLocal.valor !=
                            parcelaRemota.valor ||

                            parcelaLocal.mesFatura !=
                            parcelaRemota.mesFatura ||

                            parcelaLocal.anoFatura !=
                            parcelaRemota.anoFatura ||

                            parcelaLocal.quitadaAnteriormente !=
                            parcelaRemota.quitadaAnteriormente


                if (precisaAtualizar) {

                    parcelaCartaoDao.atualizarDaNuvem(
                        id =
                            parcelaLocal.id,

                        movimentacaoId =
                            movimentacaoLocal.id,

                        cartaoId =
                            cartaoLocal.id,

                        numeroParcela =
                            parcelaRemota.numeroParcela,

                        totalParcelas =
                            parcelaRemota.totalParcelas,

                        valor =
                            parcelaRemota.valor,

                        mesFatura =
                            parcelaRemota.mesFatura,

                        anoFatura =
                            parcelaRemota.anoFatura,

                        quitadaAnteriormente =
                            parcelaRemota.quitadaAnteriormente
                    )


                    quantidadeAlterada++
                }
            }
        }


        return quantidadeAlterada
    }

    suspend fun excluir(
        syncId: String
    ) {

        SupabaseProvider.client
            .from("parcelas_cartao")
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