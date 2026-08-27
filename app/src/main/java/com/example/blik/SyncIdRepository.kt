package com.example.blik

import java.util.UUID

object SyncIdRepository {

    suspend fun preencherContas(
        contaDao: ContaDao
    ): Int {

        val contasSemSyncId =
            contaDao.listarSemSyncId()

        var quantidadeAtualizada = 0

        contasSemSyncId.forEach { conta ->

            val atualizados =
                contaDao.definirSyncId(
                    id = conta.id,
                    syncId = UUID
                        .randomUUID()
                        .toString()
                )

            quantidadeAtualizada +=
                atualizados
        }

        return quantidadeAtualizada
    }
    suspend fun preencherCategorias(
        categoriaDao: CategoriaDao
    ): Int {

        val registros =
            categoriaDao.listarSemSyncId()

        var quantidade = 0

        registros.forEach { item ->

            quantidade +=
                categoriaDao.definirSyncId(
                    id = item.id,
                    syncId = UUID.randomUUID().toString()
                )
        }

        return quantidade
    }


    suspend fun preencherCartoes(
        cartaoDao: CartaoDao
    ): Int {

        val registros =
            cartaoDao.listarSemSyncId()

        var quantidade = 0

        registros.forEach { item ->

            quantidade +=
                cartaoDao.definirSyncId(
                    id = item.id,
                    syncId = UUID.randomUUID().toString()
                )
        }

        return quantidade
    }


    suspend fun preencherMovimentacoes(
        movimentacaoDao: MovimentacaoDao
    ): Int {

        val registros =
            movimentacaoDao.listarSemSyncId()

        var quantidade = 0

        registros.forEach { item ->

            quantidade +=
                movimentacaoDao.definirSyncId(
                    id = item.id,
                    syncId = UUID.randomUUID().toString()
                )
        }

        return quantidade
    }


    suspend fun preencherParcelas(
        parcelaCartaoDao: ParcelaCartaoDao
    ): Int {

        val registros =
            parcelaCartaoDao.listarSemSyncId()

        var quantidade = 0

        registros.forEach { item ->

            quantidade +=
                parcelaCartaoDao.definirSyncId(
                    id = item.id,
                    syncId = UUID.randomUUID().toString()
                )
        }

        return quantidade
    }


    suspend fun preencherPagamentos(
        pagamentoFaturaDao: PagamentoFaturaDao
    ): Int {

        val registros =
            pagamentoFaturaDao.listarSemSyncId()

        var quantidade = 0

        registros.forEach { item ->

            quantidade +=
                pagamentoFaturaDao.definirSyncId(
                    id = item.id,
                    syncId = UUID.randomUUID().toString()
                )
        }

        return quantidade
    }
}