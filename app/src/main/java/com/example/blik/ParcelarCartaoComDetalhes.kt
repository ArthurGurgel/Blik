package com.example.blik

data class ParcelaCartaoComDetalhes(
    val id: Int,
    val movimentacaoId: Int,
    val descricao: String,
    val cartaoId: Int,
    val cartaoNome: String,
    val numeroParcela: Int,
    val totalParcelas: Int,
    val valor: Double,
    val mesFatura: Int,
    val anoFatura: Int
)