package com.example.blik

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movimentacoes",
    foreignKeys = [
        ForeignKey(
            entity = ContaEntity::class,
            parentColumns = ["id"],
            childColumns = ["contaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["contaId"])
    ]
)
data class MovimentacaoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val descricao: String,
    val valor: Double,
    val tipo: String,
    val contaId: Int,
    val categoria: String,
    val data: String
)