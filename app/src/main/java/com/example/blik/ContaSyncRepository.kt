package com.example.blik

import io.github.jan.supabase.postgrest.from
object ContaSyncRepository {

    suspend fun sincronizar(
        conta: ContaEntity,
        usuarioId: String
    ) {

        val syncId =
            conta.syncId
                ?: throw IllegalStateException(
                    "A conta \"${conta.nome}\" não possui syncId."
                )

        ContaRemotaRepository.sincronizar(
            ContaRemotaNova(
                id = syncId,
                userId = usuarioId,
                nome = conta.nome,
                saldoInicial = conta.saldoInicial,
                ativa = conta.ativa
            )
        )
    }
    suspend fun sincronizarTodas(
        contas: List<ContaEntity>,
        usuarioId: String
    ) {
        contas.forEach { conta ->
            sincronizar(
                conta = conta,
                usuarioId = usuarioId
            )
        }
    }

    suspend fun baixarTodasParaRoom(
        contaDao: ContaDao
    ): Int {

        val contasRemotas =
            ContaRemotaRepository.listar()


        if (contasRemotas.isEmpty()) {
            return 0
        }


        var quantidadeAlterada = 0


        contasRemotas.forEach { contaRemota ->

            val contaLocal =
                contaDao.buscarPorSyncId(
                    syncId =
                        contaRemota.id
                )


            if (contaLocal == null) {

                // =============================================
                // CONTA EXISTE NA NUVEM,
                // MAS AINDA NÃO EXISTE NESTE APARELHO
                // =============================================

                val resultado =
                    contaDao.inserir(
                        ContaEntity(
                            nome =
                                contaRemota.nome,

                            saldoInicial =
                                contaRemota.saldoInicial,

                            ativa =
                                contaRemota.ativa,

                            syncId =
                                contaRemota.id
                        )
                    )


                if (resultado == -1L) {

                    throw IllegalStateException(
                        "Não foi possível importar a conta \"${contaRemota.nome}\"."
                    )
                }


                quantidadeAlterada++

            } else {

                // =============================================
                // CONTA JÁ EXISTE LOCALMENTE.
                // VERIFICA SE A NUVEM TEM DADOS DIFERENTES.
                // =============================================

                val precisaAtualizar =
                    contaLocal.nome !=
                            contaRemota.nome ||

                            contaLocal.saldoInicial !=
                            contaRemota.saldoInicial ||

                            contaLocal.ativa !=
                            contaRemota.ativa


                if (precisaAtualizar) {

                    val linhasAtualizadas =
                        contaDao.atualizarDaNuvem(
                            syncId =
                                contaRemota.id,

                            nome =
                                contaRemota.nome,

                            saldoInicial =
                                contaRemota.saldoInicial,

                            ativa =
                                contaRemota.ativa
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
            .from("contas")
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