package com.example.blik

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class OrcamentoRemotoNovo(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    val referencia: String,

    val mes: Int,

    val ano: Int,

    val tipo: String,

    @SerialName("categoria_id")
    val categoriaId: String?,

    @SerialName("limite_mensal")
    val limiteMensal: Double,

    val ativo: Boolean
)


@Serializable
data class OrcamentoRemoto(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    val referencia: String,

    val mes: Int,

    val ano: Int,

    val tipo: String,

    @SerialName("categoria_id")
    val categoriaId: String?,

    @SerialName("limite_mensal")
    val limiteMensal: Double,

    val ativo: Boolean
)