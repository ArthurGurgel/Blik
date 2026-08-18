package com.example.blik

data class MovimentacaoComConta(
    val id: Int,
    val descricao: String,
    val valor: Double,
    val tipo: String,
    val contaId: Int,
    val contaNome: String,
    val categoria: String,
    val data: String
)