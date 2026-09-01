package com.example.blik

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SyncTombstoneRemoto(

    @SerialName("user_id")
    val userId: String,

    @SerialName("tipo_entidade")
    val tipoEntidade: String,

    @SerialName("registro_id")
    val registroId: String,

    @SerialName("excluido_em")
    val excluidoEm: String
)