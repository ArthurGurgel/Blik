package com.example.blik

data class MovimentacaoComConta(
    val id: Int,
    val descricao: String,
    val valor: Double,
    val tipo: String,
    val contaId: Int,
    val contaNome: String,
    val categoriaId: Int,
    val categoriaNome: String,
    val data: String
)