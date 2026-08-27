package com.example.blik

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PagamentoFaturaRemotoNovo(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("cartao_id")
    val cartaoId: String,

    @SerialName("conta_id")
    val contaId: String,

    @SerialName("mes_fatura")
    val mesFatura: Int,

    @SerialName("ano_fatura")
    val anoFatura: Int,

    @SerialName("valor_pago")
    val valorPago: Double,

    @SerialName("data_pagamento")
    val dataPagamento: String
)


@Serializable
data class PagamentoFaturaRemoto(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("cartao_id")
    val cartaoId: String,

    @SerialName("conta_id")
    val contaId: String,

    @SerialName("mes_fatura")
    val mesFatura: Int,

    @SerialName("ano_fatura")
    val anoFatura: Int,

    @SerialName("valor_pago")
    val valorPago: Double,

    @SerialName("data_pagamento")
    val dataPagamento: String
)