package com.example.blik

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
}