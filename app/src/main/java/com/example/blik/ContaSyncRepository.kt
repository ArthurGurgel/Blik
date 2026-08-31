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

        // Neste primeiro estágio, o download automático
        // só acontece em um banco local realmente vazio.
        if (contaDao.quantidade() > 0) {
            return 0
        }

        val contasRemotas =
            ContaRemotaRepository.listar()

        if (contasRemotas.isEmpty()) {
            return 0
        }

        val contasLocais =
            contasRemotas.map { contaRemota ->

                ContaEntity(
                    nome = contaRemota.nome,
                    saldoInicial = contaRemota.saldoInicial,
                    ativa = contaRemota.ativa,

                    // Fundamental:
                    // mantém no Room o mesmo UUID do Supabase.
                    syncId = contaRemota.id
                )
            }

        contaDao.inserirTodas(
            contasLocais
        )

        return contasLocais.size
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