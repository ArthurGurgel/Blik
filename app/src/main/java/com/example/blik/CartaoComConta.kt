package com.example.blik

data class CartaoComConta(
    val id: Int,
    val nome: String,
    val limite: Double,
    val diaFechamento: Int,
    val diaVencimento: Int,
    val contaId: Int,
    val contaNome: String
)