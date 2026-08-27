package com.example.blik

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartaoRemotoNovo(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    val nome: String,

    val limite: Double,

    @SerialName("dia_fechamento")
    val diaFechamento: Int,

    @SerialName("dia_vencimento")
    val diaVencimento: Int,

    @SerialName("conta_id")
    val contaId: String
)


@Serializable
data class CartaoRemoto(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    val nome: String,

    val limite: Double,

    @SerialName("dia_fechamento")
    val diaFechamento: Int,

    @SerialName("dia_vencimento")
    val diaVencimento: Int,

    @SerialName("conta_id")
    val contaId: String
)