package com.example.blik

data class PagamentoFaturaComConta(
    val id: Int,
    val cartaoId: Int,
    val contaId: Int,
    val contaNome: String,
    val mesFatura: Int,
    val anoFatura: Int,
    val valorPago: Double,
    val dataPagamento: String
)