package com.example.blik

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParcelaCartaoRemotaNova(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("movimentacao_id")
    val movimentacaoId: String,

    @SerialName("cartao_id")
    val cartaoId: String,

    @SerialName("numero_parcela")
    val numeroParcela: Int,

    @SerialName("total_parcelas")
    val totalParcelas: Int,

    val valor: Double,

    @SerialName("mes_fatura")
    val mesFatura: Int,

    @SerialName("ano_fatura")
    val anoFatura: Int,

    @SerialName("quitada_anteriormente")
    val quitadaAnteriormente: Boolean
)


@Serializable
data class ParcelaCartaoRemota(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("movimentacao_id")
    val movimentacaoId: String,

    @SerialName("cartao_id")
    val cartaoId: String,

    @SerialName("numero_parcela")
    val numeroParcela: Int,

    @SerialName("total_parcelas")
    val totalParcelas: Int,

    val valor: Double,

    @SerialName("mes_fatura")
    val mesFatura: Int,

    @SerialName("ano_fatura")
    val anoFatura: Int,

    @SerialName("quitada_anteriormente")
    val quitadaAnteriormente: Boolean
)