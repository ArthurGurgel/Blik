package com.example.blik

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pagamentos_fatura",

    foreignKeys = [
        ForeignKey(
            entity = CartaoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cartaoId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = ContaEntity::class,
            parentColumns = ["id"],
            childColumns = ["contaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["cartaoId"]),
        Index(value = ["contaId"]),
        Index(
            value = [
                "cartaoId",
                "mesFatura",
                "anoFatura"
            ]
        ),
        Index(
            value = ["syncId"],
            unique = true
        )
    ]
)
data class PagamentoFaturaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val cartaoId: Int,

    val contaId: Int,

    val mesFatura: Int,

    val anoFatura: Int,

    val valorPago: Double,

    val dataPagamento: String,
    val syncId: String? =
        java.util.UUID.randomUUID().toString()
)