package com.example.blik

data class MovimentacaoComConta(

    val id: Int,

    val descricao: String,

    val valor: Double,

    val tipo: String,

    val formaPagamento: String?,

    val contaId: Int?,

    val contaNome: String?,

    val contaDestinoId: Int?,

    val contaDestinoNome: String?,

    val categoriaId: Int?,

    val categoriaNome: String?,

    val cartaoId: Int?,

    val cartaoNome: String?,

    val quantidadeParcelas: Int,

    val data: String
)