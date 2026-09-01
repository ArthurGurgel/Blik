package com.example.blik

import io.github.jan.supabase.postgrest.from


object SyncTombstoneRepository {


    suspend fun listar():
            List<SyncTombstoneRemoto> {

        return SupabaseProvider.client
            .from("sync_tombstones")
            .select()
            .decodeList<SyncTombstoneRemoto>()
    }


    suspend fun aplicarNoRoom(

        contaDao: ContaDao,

        categoriaDao: CategoriaDao,

        cartaoDao: CartaoDao,

        movimentacaoDao: MovimentacaoDao,

        parcelaCartaoDao: ParcelaCartaoDao,

        pagamentoFaturaDao:
        PagamentoFaturaDao

    ): Int {


        val exclusoes =
            listar()


        if (exclusoes.isEmpty()) {
            return 0
        }


        var quantidadeExcluida =
            0


        // =============================================
        // FILHOS PRIMEIRO
        // =============================================


        exclusoes
            .filter {
                it.tipoEntidade ==
                        "pagamentos_fatura"
            }
            .forEach { item ->

                quantidadeExcluida +=
                    pagamentoFaturaDao
                        .excluirPorSyncId(
                            item.registroId
                        )
            }


        exclusoes
            .filter {
                it.tipoEntidade ==
                        "parcelas_cartao"
            }
            .forEach { item ->

                quantidadeExcluida +=
                    parcelaCartaoDao
                        .excluirPorSyncId(
                            item.registroId
                        )
            }


        exclusoes
            .filter {
                it.tipoEntidade ==
                        "movimentacoes"
            }
            .forEach { item ->

                quantidadeExcluida +=
                    movimentacaoDao
                        .excluirPorSyncId(
                            item.registroId
                        )
            }


        exclusoes
            .filter {
                it.tipoEntidade ==
                        "cartoes"
            }
            .forEach { item ->

                quantidadeExcluida +=
                    cartaoDao
                        .excluirPorSyncId(
                            item.registroId
                        )
            }


        exclusoes
            .filter {
                it.tipoEntidade ==
                        "categorias"
            }
            .forEach { item ->

                quantidadeExcluida +=
                    categoriaDao
                        .excluirPorSyncId(
                            item.registroId
                        )
            }


        exclusoes
            .filter {
                it.tipoEntidade ==
                        "contas"
            }
            .forEach { item ->

                quantidadeExcluida +=
                    contaDao
                        .excluirPorSyncId(
                            item.registroId
                        )
            }


        return quantidadeExcluida
    }
}