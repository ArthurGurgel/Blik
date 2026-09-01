package com.example.blik

import io.github.jan.supabase.postgrest.from
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PagamentoFaturaSyncRepository {

    private val formatoLocal =
        DateTimeFormatter.ofPattern(
            "dd/MM/yyyy"
        )


    // =============================================
    // ROOM -> SUPABASE
    // SINCRONIZA UM PAGAMENTO
    // =============================================

    suspend fun sincronizar(
        pagamento: PagamentoFaturaEntity,
        contas: List<ContaEntity>,
        cartoes: List<CartaoEntity>,
        usuarioId: String
    ) {

        val syncId =
            pagamento.syncId
                ?: throw IllegalStateException(
                    "O pagamento de fatura não possui syncId."
                )


        val conta =
            contas
                .firstOrNull { conta ->
                    conta.id ==
                            pagamento.contaId
                }
                ?: throw IllegalStateException(
                    "Conta do pagamento de fatura não encontrada."
                )

        val contaSyncId =
            conta.syncId
                ?: throw IllegalStateException(
                    "A conta \"${conta.nome}\" não possui syncId."
                )


        val cartao =
            cartoes
                .firstOrNull { cartao ->
                    cartao.id ==
                            pagamento.cartaoId
                }
                ?: throw IllegalStateException(
                    "Cartão do pagamento de fatura não encontrado."
                )

        val cartaoSyncId =
            cartao.syncId
                ?: throw IllegalStateException(
                    "O cartão \"${cartao.nome}\" não possui syncId."
                )


        val dataIso =
            LocalDate
                .parse(
                    pagamento.dataPagamento,
                    formatoLocal
                )
                .toString()


        SupabaseProvider.client
            .from("pagamentos_fatura")
            .upsert(
                PagamentoFaturaRemotoNovo(
                    id =
                        syncId,

                    userId =
                        usuarioId,

                    cartaoId =
                        cartaoSyncId,

                    contaId =
                        contaSyncId,

                    mesFatura =
                        pagamento.mesFatura,

                    anoFatura =
                        pagamento.anoFatura,

                    valorPago =
                        pagamento.valorPago,

                    dataPagamento =
                        dataIso
                )
            )
    }


    // =============================================
    // ROOM -> SUPABASE
    // SINCRONIZA TODOS
    // =============================================

    suspend fun sincronizarTodos(
        pagamentos: List<PagamentoFaturaEntity>,
        contas: List<ContaEntity>,
        cartoes: List<CartaoEntity>,
        usuarioId: String
    ) {

        pagamentos.forEach { pagamento ->

            sincronizar(
                pagamento = pagamento,
                contas = contas,
                cartoes = cartoes,
                usuarioId = usuarioId
            )
        }
    }


    // =============================================
    // SUPABASE -> ROOM
    // DISPOSITIVO NOVO
    // =============================================

    suspend fun baixarTodosParaRoom(
        pagamentoFaturaDao: PagamentoFaturaDao,
        contaDao: ContaDao,
        cartaoDao: CartaoDao
    ): Int {

        val pagamentosRemotos =
            PagamentoFaturaRemotoRepository.listar()


        if (pagamentosRemotos.isEmpty()) {
            return 0
        }


        val contasLocais =
            contaDao.listarTodasUmaVez()

        val cartoesLocais =
            cartaoDao.listarTodosUmaVez()


        var quantidadeAlterada = 0


        pagamentosRemotos.forEach { pagamentoRemoto ->

            val contaLocal =
                contasLocais
                    .firstOrNull { conta ->
                        conta.syncId ==
                                pagamentoRemoto.contaId
                    }
                    ?: throw IllegalStateException(
                        "Conta do pagamento de fatura não encontrada."
                    )


            val cartaoLocal =
                cartoesLocais
                    .firstOrNull { cartao ->
                        cartao.syncId ==
                                pagamentoRemoto.cartaoId
                    }
                    ?: throw IllegalStateException(
                        "Cartão do pagamento de fatura não encontrado."
                    )


            val dataLocal =
                LocalDate
                    .parse(
                        pagamentoRemoto.dataPagamento
                    )
                    .format(
                        formatoLocal
                    )


            val pagamentoLocal =
                pagamentoFaturaDao.buscarPorSyncId(
                    syncId =
                        pagamentoRemoto.id
                )


            if (pagamentoLocal == null) {

                pagamentoFaturaDao.inserir(
                    PagamentoFaturaEntity(
                        cartaoId =
                            cartaoLocal.id,

                        contaId =
                            contaLocal.id,

                        mesFatura =
                            pagamentoRemoto.mesFatura,

                        anoFatura =
                            pagamentoRemoto.anoFatura,

                        valorPago =
                            pagamentoRemoto.valorPago,

                        dataPagamento =
                            dataLocal,

                        syncId =
                            pagamentoRemoto.id
                    )
                )


                quantidadeAlterada++

            } else {

                val precisaAtualizar =
                    pagamentoLocal.cartaoId !=
                            cartaoLocal.id ||

                            pagamentoLocal.contaId !=
                            contaLocal.id ||

                            pagamentoLocal.mesFatura !=
                            pagamentoRemoto.mesFatura ||

                            pagamentoLocal.anoFatura !=
                            pagamentoRemoto.anoFatura ||

                            pagamentoLocal.valorPago !=
                            pagamentoRemoto.valorPago ||

                            pagamentoLocal.dataPagamento !=
                            dataLocal


                if (precisaAtualizar) {

                    pagamentoFaturaDao.atualizarDaNuvem(
                        id =
                            pagamentoLocal.id,

                        cartaoId =
                            cartaoLocal.id,

                        contaId =
                            contaLocal.id,

                        mesFatura =
                            pagamentoRemoto.mesFatura,

                        anoFatura =
                            pagamentoRemoto.anoFatura,

                        valorPago =
                            pagamentoRemoto.valorPago,

                        dataPagamento =
                            dataLocal
                    )


                    quantidadeAlterada++
                }
            }
        }


        return quantidadeAlterada
    }

    // =============================================
// EXCLUSÃO
// SUPABASE
// =============================================

    suspend fun excluir(
        syncId: String
    ) {

        SupabaseProvider.client
            .from("pagamentos_fatura")
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