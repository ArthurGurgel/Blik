package com.example.blik

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovimentacaoRemotaNova(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    val descricao: String,

    val valor: Double,

    val tipo: String,

    @SerialName("forma_pagamento")
    val formaPagamento: String? = null,

    @SerialName("conta_id")
    val contaId: String? = null,

    @SerialName("conta_destino_id")
    val contaDestinoId: String? = null,

    @SerialName("categoria_id")
    val categoriaId: String? = null,

    @SerialName("cartao_id")
    val cartaoId: String? = null,

    @SerialName("quantidade_parcelas")
    val quantidadeParcelas: Int,

    val data: String
)


@Serializable
data class MovimentacaoRemota(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    val descricao: String,

    val valor: Double,

    val tipo: String,

    @SerialName("forma_pagamento")
    val formaPagamento: String? = null,

    @SerialName("conta_id")
    val contaId: String? = null,

    @SerialName("conta_destino_id")
    val contaDestinoId: String? = null,

    @SerialName("categoria_id")
    val categoriaId: String? = null,

    @SerialName("cartao_id")
    val cartaoId: String? = null,

    @SerialName("quantidade_parcelas")
    val quantidadeParcelas: Int,

    val data: String
)