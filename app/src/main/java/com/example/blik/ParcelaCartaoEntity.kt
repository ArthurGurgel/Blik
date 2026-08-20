package com.example.blik

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parcelas_cartao",

    foreignKeys = [
        ForeignKey(
            entity = MovimentacaoEntity::class,
            parentColumns = ["id"],
            childColumns = ["movimentacaoId"],
            onDelete = ForeignKey.CASCADE
        ),

        ForeignKey(
            entity = CartaoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cartaoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["movimentacaoId"]),
        Index(value = ["cartaoId"]),

        Index(
            value = [
                "movimentacaoId",
                "numeroParcela"
            ],
            unique = true
        )
    ]
)
data class ParcelaCartaoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val movimentacaoId: Int,

    val cartaoId: Int,

    val numeroParcela: Int,

    val totalParcelas: Int,

    val valor: Double,

    val mesFatura: Int,

    val anoFatura: Int
)