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

        // Só faz a carga inicial em um banco local
        // que ainda não possui cartões.
        if (cartaoDao.listarTodosUmaVez().isNotEmpty()) {
            return 0
        }

        val cartoesRemotos =
            CartaoRemotoRepository.listar()

        if (cartoesRemotos.isEmpty()) {
            throw IllegalStateException(
                "O Supabase retornou 0 cartões."
            )
        }

        if (cartoesRemotos.isEmpty()) {
            return 0
        }

        val contasLocais =
            contaDao.listarTodasUmaVez()


        cartoesRemotos.forEach { cartaoRemoto ->

            val contaLocal =
                contasLocais.firstOrNull { conta ->
                    conta.syncId == cartaoRemoto.contaId
                }
                    ?: throw IllegalStateException(
                        "Não foi encontrada a conta local " +
                                "do cartão \"${cartaoRemoto.nome}\"."
                    )


            cartaoDao.inserir(
                CartaoEntity(
                    nome = cartaoRemoto.nome,
                    limite = cartaoRemoto.limite,
                    diaFechamento = cartaoRemoto.diaFechamento,
                    diaVencimento = cartaoRemoto.diaVencimento,

                    // UUID remoto da conta virou
                    // o Int local correspondente.
                    contaId = contaLocal.id,

                    // Mantém o mesmo UUID do cartão.
                    syncId = cartaoRemoto.id
                )
            )
        }

        return cartoesRemotos.size
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