package com.example.blik

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
}