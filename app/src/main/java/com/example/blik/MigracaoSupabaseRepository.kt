package com.example.blik

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object MigracaoSupabaseRepository {

    suspend fun migrarContas(
        contas: List<ContaEntity>,
        usuarioId: String
    ): Int {

        val contasRemotas =
            ContaRemotaRepository.listar()

        var quantidadeEnviada = 0

        contas.forEach { contaLocal ->

            val syncId =
                contaLocal.syncId
                    ?: throw IllegalStateException(
                        "A conta \"${contaLocal.nome}\" não possui syncId."
                    )

            val existentePorId =
                contasRemotas.firstOrNull { contaRemota ->
                    contaRemota.id == syncId
                }

            if (existentePorId != null) {
                // Esta conta já foi migrada.
                return@forEach
            }

            val conflitoPorNome =
                contasRemotas.firstOrNull { contaRemota ->
                    contaRemota.nome == contaLocal.nome
                }

            if (conflitoPorNome != null) {
                throw IllegalStateException(
                    "Já existe no Supabase uma conta chamada " +
                            "\"${contaLocal.nome}\" com outro identificador."
                )
            }

            ContaRemotaRepository.inserir(
                ContaRemotaNova(
                    id = syncId,
                    userId = usuarioId,
                    nome = contaLocal.nome,
                    saldoInicial = contaLocal.saldoInicial,
                    ativa = contaLocal.ativa
                )
            )

            quantidadeEnviada++
        }

        return quantidadeEnviada
    }

    suspend fun migrarCategorias(
        categorias: List<CategoriaEntity>,
        usuarioId: String
    ): Int {

        val categoriasRemotas =
            CategoriaRemotaRepository.listar()

        var quantidadeEnviada = 0

        categorias.forEach { categoriaLocal ->

            val syncId =
                categoriaLocal.syncId
                    ?: throw IllegalStateException(
                        "A categoria \"${categoriaLocal.nome}\" não possui syncId."
                    )

            val existentePorId =
                categoriasRemotas.firstOrNull { categoriaRemota ->
                    categoriaRemota.id == syncId
                }

            if (existentePorId != null) {
                return@forEach
            }

            val conflitoPorNome =
                categoriasRemotas.firstOrNull { categoriaRemota ->
                    categoriaRemota.nome == categoriaLocal.nome
                }

            if (conflitoPorNome != null) {
                throw IllegalStateException(
                    "Já existe no Supabase uma categoria chamada " +
                            "\"${categoriaLocal.nome}\" com outro identificador."
                )
            }

            CategoriaRemotaRepository.inserir(
                CategoriaRemotaNova(
                    id = syncId,
                    userId = usuarioId,
                    nome = categoriaLocal.nome
                )
            )

            quantidadeEnviada++
        }

        return quantidadeEnviada
    }
    suspend fun migrarCartoes(
        cartoes: List<CartaoEntity>,
        contas: List<ContaEntity>,
        usuarioId: String
    ): Int {

        val cartoesRemotos =
            CartaoRemotoRepository.listar()

        val contasPorId =
            contas.associateBy { conta ->
                conta.id
            }

        var quantidadeEnviada = 0

        cartoes.forEach { cartaoLocal ->

            val syncId =
                cartaoLocal.syncId
                    ?: throw IllegalStateException(
                        "O cartão \"${cartaoLocal.nome}\" não possui syncId."
                    )


            val contaLocal =
                contasPorId[cartaoLocal.contaId]
                    ?: throw IllegalStateException(
                        "A conta vinculada ao cartão " +
                                "\"${cartaoLocal.nome}\" não foi encontrada."
                    )


            val contaSyncId =
                contaLocal.syncId
                    ?: throw IllegalStateException(
                        "A conta \"${contaLocal.nome}\" " +
                                "não possui syncId."
                    )


            val existentePorId =
                cartoesRemotos.firstOrNull { cartaoRemoto ->
                    cartaoRemoto.id == syncId
                }

            if (existentePorId != null) {
                return@forEach
            }


            val conflitoPorNome =
                cartoesRemotos.firstOrNull { cartaoRemoto ->
                    cartaoRemoto.nome == cartaoLocal.nome
                }

            if (conflitoPorNome != null) {
                throw IllegalStateException(
                    "Já existe no Supabase um cartão chamado " +
                            "\"${cartaoLocal.nome}\" com outro identificador."
                )
            }


            CartaoRemotoRepository.inserir(
                CartaoRemotoNovo(
                    id = syncId,
                    userId = usuarioId,
                    nome = cartaoLocal.nome,
                    limite = cartaoLocal.limite,
                    diaFechamento = cartaoLocal.diaFechamento,
                    diaVencimento = cartaoLocal.diaVencimento,
                    contaId = contaSyncId
                )
            )

            quantidadeEnviada++
        }

        return quantidadeEnviada
    }

    suspend fun migrarMovimentacoes(
        movimentacoes: List<MovimentacaoEntity>,
        contas: List<ContaEntity>,
        categorias: List<CategoriaEntity>,
        cartoes: List<CartaoEntity>,
        usuarioId: String
    ): Int {

        val movimentacoesRemotas =
            MovimentacaoRemotaRepository.listar()

        val contasPorId =
            contas.associateBy { it.id }

        val categoriasPorId =
            categorias.associateBy { it.id }

        val cartoesPorId =
            cartoes.associateBy { it.id }


        val formatoLocal =
            DateTimeFormatter.ofPattern(
                "dd/MM/yyyy"
            )


        fun contaSyncId(
            contaId: Int?
        ): String? {

            if (contaId == null) {
                return null
            }

            val conta =
                contasPorId[contaId]
                    ?: throw IllegalStateException(
                        "Conta local $contaId não encontrada."
                    )

            return conta.syncId
                ?: throw IllegalStateException(
                    "A conta \"${conta.nome}\" não possui syncId."
                )
        }


        fun categoriaSyncId(
            categoriaId: Int?
        ): String? {

            if (categoriaId == null) {
                return null
            }

            val categoria =
                categoriasPorId[categoriaId]
                    ?: throw IllegalStateException(
                        "Categoria local $categoriaId não encontrada."
                    )

            return categoria.syncId
                ?: throw IllegalStateException(
                    "A categoria \"${categoria.nome}\" não possui syncId."
                )
        }


        fun cartaoSyncId(
            cartaoId: Int?
        ): String? {

            if (cartaoId == null) {
                return null
            }

            val cartao =
                cartoesPorId[cartaoId]
                    ?: throw IllegalStateException(
                        "Cartão local $cartaoId não encontrado."
                    )

            return cartao.syncId
                ?: throw IllegalStateException(
                    "O cartão \"${cartao.nome}\" não possui syncId."
                )
        }


        var quantidadeEnviada = 0


        movimentacoes.forEach { movimentacaoLocal ->

            val syncId =
                movimentacaoLocal.syncId
                    ?: throw IllegalStateException(
                        "A movimentação " +
                                "\"${movimentacaoLocal.descricao}\" " +
                                "não possui syncId."
                    )


            val jaExiste =
                movimentacoesRemotas.any {
                    it.id == syncId
                }

            if (jaExiste) {
                return@forEach
            }


            val dataSupabase =
                try {

                    LocalDate
                        .parse(
                            movimentacaoLocal.data,
                            formatoLocal
                        )
                        .toString()

                } catch (e: Exception) {

                    throw IllegalStateException(
                        "Data inválida na movimentação " +
                                "\"${movimentacaoLocal.descricao}\": " +
                                movimentacaoLocal.data
                    )
                }


            MovimentacaoRemotaRepository.inserir(
                MovimentacaoRemotaNova(
                    id = syncId,
                    userId = usuarioId,

                    descricao =
                        movimentacaoLocal.descricao,

                    valor =
                        movimentacaoLocal.valor,

                    tipo =
                        movimentacaoLocal.tipo,

                    formaPagamento =
                        movimentacaoLocal.formaPagamento,

                    contaId =
                        contaSyncId(
                            movimentacaoLocal.contaId
                        ),

                    contaDestinoId =
                        contaSyncId(
                            movimentacaoLocal.contaDestinoId
                        ),

                    categoriaId =
                        categoriaSyncId(
                            movimentacaoLocal.categoriaId
                        ),

                    cartaoId =
                        cartaoSyncId(
                            movimentacaoLocal.cartaoId
                        ),

                    quantidadeParcelas =
                        movimentacaoLocal.quantidadeParcelas,

                    data =
                        dataSupabase
                )
            )

            quantidadeEnviada++
        }


        return quantidadeEnviada
    }

    suspend fun migrarParcelas(
        parcelas: List<ParcelaCartaoEntity>,
        movimentacoes: List<MovimentacaoEntity>,
        cartoes: List<CartaoEntity>,
        usuarioId: String
    ): Int {

        val parcelasRemotas =
            ParcelaCartaoRemotaRepository.listar()

        val movimentacoesPorId =
            movimentacoes.associateBy { it.id }

        val cartoesPorId =
            cartoes.associateBy { it.id }

        var quantidadeEnviada = 0


        parcelas.forEach { parcelaLocal ->

            val syncId =
                parcelaLocal.syncId
                    ?: throw IllegalStateException(
                        "A parcela ${parcelaLocal.numeroParcela} " +
                                "não possui syncId."
                    )


            val jaExiste =
                parcelasRemotas.any {
                    it.id == syncId
                }

            if (jaExiste) {
                return@forEach
            }


            val movimentacaoLocal =
                movimentacoesPorId[
                    parcelaLocal.movimentacaoId
                ]
                    ?: throw IllegalStateException(
                        "Movimentação local " +
                                "${parcelaLocal.movimentacaoId} " +
                                "da parcela não encontrada."
                    )

            val movimentacaoSyncId =
                movimentacaoLocal.syncId
                    ?: throw IllegalStateException(
                        "A movimentação " +
                                "\"${movimentacaoLocal.descricao}\" " +
                                "não possui syncId."
                    )


            val cartaoLocal =
                cartoesPorId[
                    parcelaLocal.cartaoId
                ]
                    ?: throw IllegalStateException(
                        "Cartão local " +
                                "${parcelaLocal.cartaoId} " +
                                "da parcela não encontrado."
                    )

            val cartaoSyncId =
                cartaoLocal.syncId
                    ?: throw IllegalStateException(
                        "O cartão \"${cartaoLocal.nome}\" " +
                                "não possui syncId."
                    )


            val conflito =
                parcelasRemotas.firstOrNull {
                    it.movimentacaoId ==
                            movimentacaoSyncId &&
                            it.numeroParcela ==
                            parcelaLocal.numeroParcela
                }

            if (conflito != null) {

                throw IllegalStateException(
                    "Já existe no Supabase a parcela " +
                            "${parcelaLocal.numeroParcela} " +
                            "desta movimentação com outro identificador."
                )
            }


            ParcelaCartaoRemotaRepository.inserir(
                ParcelaCartaoRemotaNova(
                    id = syncId,
                    userId = usuarioId,

                    movimentacaoId =
                        movimentacaoSyncId,

                    cartaoId =
                        cartaoSyncId,

                    numeroParcela =
                        parcelaLocal.numeroParcela,

                    totalParcelas =
                        parcelaLocal.totalParcelas,

                    valor =
                        parcelaLocal.valor,

                    mesFatura =
                        parcelaLocal.mesFatura,

                    anoFatura =
                        parcelaLocal.anoFatura,

                    quitadaAnteriormente =
                        parcelaLocal.quitadaAnteriormente
                )
            )

            quantidadeEnviada++
        }

        return quantidadeEnviada
    }

    suspend fun migrarPagamentos(
        pagamentos: List<PagamentoFaturaEntity>,
        contas: List<ContaEntity>,
        cartoes: List<CartaoEntity>,
        usuarioId: String
    ): Int {

        val pagamentosRemotos =
            PagamentoFaturaRemotoRepository.listar()

        val contasPorId =
            contas.associateBy { it.id }

        val cartoesPorId =
            cartoes.associateBy { it.id }

        val formatoLocal =
            DateTimeFormatter.ofPattern(
                "dd/MM/yyyy"
            )

        var quantidadeEnviada = 0


        pagamentos.forEach { pagamentoLocal ->

            val syncId =
                pagamentoLocal.syncId
                    ?: throw IllegalStateException(
                        "O pagamento ${pagamentoLocal.id} " +
                                "não possui syncId."
                    )


            val jaExiste =
                pagamentosRemotos.any {
                    it.id == syncId
                }

            if (jaExiste) {
                return@forEach
            }


            val contaLocal =
                contasPorId[
                    pagamentoLocal.contaId
                ]
                    ?: throw IllegalStateException(
                        "Conta local " +
                                "${pagamentoLocal.contaId} " +
                                "do pagamento não encontrada."
                    )

            val contaSyncId =
                contaLocal.syncId
                    ?: throw IllegalStateException(
                        "A conta \"${contaLocal.nome}\" " +
                                "não possui syncId."
                    )


            val cartaoLocal =
                cartoesPorId[
                    pagamentoLocal.cartaoId
                ]
                    ?: throw IllegalStateException(
                        "Cartão local " +
                                "${pagamentoLocal.cartaoId} " +
                                "do pagamento não encontrado."
                    )

            val cartaoSyncId =
                cartaoLocal.syncId
                    ?: throw IllegalStateException(
                        "O cartão \"${cartaoLocal.nome}\" " +
                                "não possui syncId."
                    )


            val dataSupabase =
                try {

                    LocalDate
                        .parse(
                            pagamentoLocal.dataPagamento,
                            formatoLocal
                        )
                        .toString()

                } catch (e: Exception) {

                    throw IllegalStateException(
                        "Data inválida no pagamento " +
                                "${pagamentoLocal.id}: " +
                                pagamentoLocal.dataPagamento
                    )
                }


            PagamentoFaturaRemotoRepository.inserir(
                PagamentoFaturaRemotoNovo(
                    id = syncId,
                    userId = usuarioId,

                    cartaoId =
                        cartaoSyncId,

                    contaId =
                        contaSyncId,

                    mesFatura =
                        pagamentoLocal.mesFatura,

                    anoFatura =
                        pagamentoLocal.anoFatura,

                    valorPago =
                        pagamentoLocal.valorPago,

                    dataPagamento =
                        dataSupabase
                )
            )

            quantidadeEnviada++
        }


        return quantidadeEnviada
    }
}