package com.example.blik

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoriaRemotaNova(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    val nome: String
)


@Serializable
data class CategoriaRemota(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    val nome: String
)