package com.example.blik

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "cartoes",
    foreignKeys = [
        ForeignKey(
            entity = ContaEntity::class,
            parentColumns = ["id"],
            childColumns = ["contaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(
            value = ["nome"],
            unique = true
        ),
        Index(
            value = ["contaId"]
        ),
        Index(
            value = ["syncId"],
            unique = true
        )
    ]
)

data class CartaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val limite: Double,
    val diaFechamento: Int,
    val diaVencimento: Int,
    val contaId: Int,
    val syncId: String? =
        java.util.UUID.randomUUID().toString()
)