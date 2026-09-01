package com.example.blik

import io.github.jan.supabase.postgrest.from

object CartaoSyncRepository {

    suspend fun sincronizar(
        cartao: CartaoEntity,
        contas: List<ContaEntity>,
        usuarioId: String
    ) {

        val syncId =
            cartao.syncId
                ?: throw IllegalStateException(
                    "O cartão \"${cartao.nome}\" não possui syncId."
                )

        val conta =
            contas.firstOrNull {
                it.id == cartao.contaId
            }
                ?: throw IllegalStateException(
                    "A conta vinculada ao cartão " +
                            "\"${cartao.nome}\" não foi encontrada."
                )

        val contaSyncId =
            conta.syncId
                ?: throw IllegalStateException(
                    "A conta \"${conta.nome}\" não possui syncId."
                )

        CartaoRemotoRepository.sincronizar(
            CartaoRemotoNovo(
                id = syncId,
                userId = usuarioId,
                nome = cartao.nome,
                limite = cartao.limite,
                diaFechamento = cartao.diaFechamento,
                diaVencimento = cartao.diaVencimento,
                contaId = contaSyncId
            )
        )
    }


    suspend fun sincronizarTodos(
        cartoes: List<CartaoEntity>,
        contas: List<ContaEntity>,
        usuarioId: String
    ) {

        cartoes.forEach { cartao ->

            sincronizar(
                cartao = cartao,
                contas = contas,
                usuarioId = usuarioId
            )
        }
    }

    suspend fun baixarTodosParaRoom(
        cartaoDao: CartaoDao,
        contaDao: ContaDao
    ): Int {

        val cartoesRemotos =
            CartaoRemotoRepository.listar()


        if (cartoesRemotos.isEmpty()) {
            return 0
        }


        val contasLocais =
            contaDao.listarTodasUmaVez()


        var quantidadeAlterada = 0


        cartoesRemotos.forEach { cartaoRemoto ->

            val contaLocal =
                contasLocais.firstOrNull { conta ->
                    conta.syncId ==
                            cartaoRemoto.contaId
                }
                    ?: throw IllegalStateException(
                        "Não foi encontrada a conta local " +
                                "do cartão \"${cartaoRemoto.nome}\"."
                    )


            val cartaoLocal =
                cartaoDao.buscarPorSyncId(
                    syncId =
                        cartaoRemoto.id
                )


            if (cartaoLocal == null) {

                val resultado =
                    cartaoDao.inserir(
                        CartaoEntity(
                            nome =
                                cartaoRemoto.nome,

                            limite =
                                cartaoRemoto.limite,

                            diaFechamento =
                                cartaoRemoto.diaFechamento,

                            diaVencimento =
                                cartaoRemoto.diaVencimento,

                            contaId =
                                contaLocal.id,

                            syncId =
                                cartaoRemoto.id
                        )
                    )


                if (resultado == -1L) {
                    throw IllegalStateException(
                        "Não foi possível importar o cartão " +
                                "\"${cartaoRemoto.nome}\"."
                    )
                }


                quantidadeAlterada++

            } else {

                val precisaAtualizar =
                    cartaoLocal.nome !=
                            cartaoRemoto.nome ||

                            cartaoLocal.limite !=
                            cartaoRemoto.limite ||

                            cartaoLocal.diaFechamento !=
                            cartaoRemoto.diaFechamento ||

                            cartaoLocal.diaVencimento !=
                            cartaoRemoto.diaVencimento ||

                            cartaoLocal.contaId !=
                            contaLocal.id


                if (precisaAtualizar) {

                    cartaoDao.editar(
                        id =
                            cartaoLocal.id,

                        nome =
                            cartaoRemoto.nome,

                        limite =
                            cartaoRemoto.limite,

                        diaFechamento =
                            cartaoRemoto.diaFechamento,

                        diaVencimento =
                            cartaoRemoto.diaVencimento,

                        contaId =
                            contaLocal.id
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
            .from("cartoes")
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