package com.example.blik

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContaRemotaNova(

    @SerialName("user_id")
    val userId: String,

    val nome: String,

    @SerialName("saldo_inicial")
    val saldoInicial: Double,

    val ativa: Boolean
)