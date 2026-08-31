package com.example.blik

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import io.github.jan.supabase.postgrest.from

object MovimentacaoSyncRepository {

    private val formatoLocal =
        DateTimeFormatter.ofPattern("dd/MM/yyyy")


    suspend fun sincronizar(
        movimentacao: MovimentacaoEntity,
        contas: List<ContaEntity>,
        categorias: List<CategoriaEntity>,
        cartoes: List<CartaoEntity>,
        usuarioId: String
    ) {

        val syncId =
            movimentacao.syncId
                ?: throw IllegalStateException(
                    "A movimentação \"${movimentacao.descricao}\" não possui syncId."
                )


        val contaSyncId =
            movimentacao.contaId?.let { contaId ->

                contas
                    .firstOrNull { conta ->
                        conta.id == contaId
                    }
                    ?.syncId
                    ?: throw IllegalStateException(
                        "Conta da movimentação \"${movimentacao.descricao}\" não encontrada."
                    )
            }


        val contaDestinoSyncId =
            movimentacao.contaDestinoId?.let { contaId ->

                contas
                    .firstOrNull { conta ->
                        conta.id == contaId
                    }
                    ?.syncId
                    ?: throw IllegalStateException(
                        "Conta de destino da movimentação \"${movimentacao.descricao}\" não encontrada."
                    )
            }


        val categoriaSyncId =
            movimentacao.categoriaId?.let { categoriaId ->

                categorias
                    .firstOrNull { categoria ->
                        categoria.id == categoriaId
                    }
                    ?.syncId
                    ?: throw IllegalStateException(
                        "Categoria da movimentação \"${movimentacao.descricao}\" não encontrada."
                    )
            }


        val cartaoSyncId =
            movimentacao.cartaoId?.let { cartaoId ->

                cartoes
                    .firstOrNull { cartao ->
                        cartao.id == cartaoId
                    }
                    ?.syncId
                    ?: throw IllegalStateException(
                        "Cartão da movimentação \"${movimentacao.descricao}\" não encontrado."
                    )
            }


        val dataRemota =
            LocalDate
                .parse(
                    movimentacao.data,
                    formatoLocal
                )
                .toString()


        MovimentacaoRemotaRepository.sincronizar(
            MovimentacaoRemotaNova(
                id = syncId,
                userId = usuarioId,
                descricao = movimentacao.descricao,
                valor = movimentacao.valor,
                tipo = movimentacao.tipo,
                formaPagamento = movimentacao.formaPagamento,
                contaId = contaSyncId,
                contaDestinoId = contaDestinoSyncId,
                categoriaId = categoriaSyncId,
                cartaoId = cartaoSyncId,
                quantidadeParcelas =
                    movimentacao.quantidadeParcelas,
                data = dataRemota
            )
        )
    }


    suspend fun sincronizarTodas(
        movimentacoes: List<MovimentacaoEntity>,
        contas: List<ContaEntity>,
        categorias: List<CategoriaEntity>,
        cartoes: List<CartaoEntity>,
        usuarioId: String
    ) {

        movimentacoes.forEach { movimentacao ->

            sincronizar(
                movimentacao = movimentacao,
                contas = contas,
                categorias = categorias,
                cartoes = cartoes,
                usuarioId = usuarioId
            )
        }
    }

    suspend fun baixarTodasParaRoom(
        movimentacaoDao: MovimentacaoDao,
        contaDao: ContaDao,
        categoriaDao: CategoriaDao,
        cartaoDao: CartaoDao
    ): Int {

        if (movimentacaoDao.listarTodasUmaVez().isNotEmpty()) {
            return 0
        }

        val movimentacoesRemotas =
            MovimentacaoRemotaRepository.listar()

        if (movimentacoesRemotas.isEmpty()) {
            return 0
        }


        val contasLocais =
            contaDao.listarTodasUmaVez()

        val categoriasLocais =
            categoriaDao.listarTodasUmaVez()

        val cartoesLocais =
            cartaoDao.listarTodosUmaVez()


        movimentacoesRemotas.forEach { movimentacaoRemota ->

            val contaLocalId =
                movimentacaoRemota.contaId?.let { contaRemotaId ->

                    contasLocais
                        .firstOrNull { conta ->
                            conta.syncId == contaRemotaId
                        }
                        ?.id
                        ?: throw IllegalStateException(
                            "Conta da movimentação " +
                                    "\"${movimentacaoRemota.descricao}\" " +
                                    "não encontrada."
                        )
                }


            val contaDestinoLocalId =
                movimentacaoRemota.contaDestinoId?.let { contaRemotaId ->

                    contasLocais
                        .firstOrNull { conta ->
                            conta.syncId == contaRemotaId
                        }
                        ?.id
                        ?: throw IllegalStateException(
                            "Conta de destino da movimentação " +
                                    "\"${movimentacaoRemota.descricao}\" " +
                                    "não encontrada."
                        )
                }


            val categoriaLocalId =
                movimentacaoRemota.categoriaId?.let { categoriaRemotaId ->

                    categoriasLocais
                        .firstOrNull { categoria ->
                            categoria.syncId == categoriaRemotaId
                        }
                        ?.id
                        ?: throw IllegalStateException(
                            "OUTROS LOCAL: ${
                                categoriasLocais
                                    .firstOrNull { it.nome == "Outros" }
                                    ?.syncId
                                    ?: "NAO EXISTE"
                            }"
                        )
                }

            val cartaoLocalId =
                movimentacaoRemota.cartaoId?.let { cartaoRemotoId ->

                    cartoesLocais
                        .firstOrNull { cartao ->
                            cartao.syncId == cartaoRemotoId
                        }
                        ?.id
                        ?: throw IllegalStateException(
                            "OUTROS LOCAL: ${
                                categoriasLocais
                                    .firstOrNull { it.nome == "Outros" }
                                    ?.syncId
                                    ?: "NAO EXISTE"
                            }"
                        )
                }


            val dataLocal =
                LocalDate
                    .parse(movimentacaoRemota.data)
                    .format(formatoLocal)


            movimentacaoDao.inserir(
                MovimentacaoEntity(
                    descricao = movimentacaoRemota.descricao,
                    valor = movimentacaoRemota.valor,
                    tipo = movimentacaoRemota.tipo,
                    formaPagamento = movimentacaoRemota.formaPagamento,

                    contaId = contaLocalId,
                    contaDestinoId = contaDestinoLocalId,
                    categoriaId = categoriaLocalId,
                    cartaoId = cartaoLocalId,

                    quantidadeParcelas =
                        movimentacaoRemota.quantidadeParcelas,

                    data = dataLocal,

                    // preserva o mesmo UUID da nuvem
                    syncId = movimentacaoRemota.id
                )
            )
        }


        return movimentacoesRemotas.size
    }

    suspend fun excluir(
        syncId: String
    ) {

        SupabaseProvider.client
            .from("movimentacoes")
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