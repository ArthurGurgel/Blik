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
        ),

        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["contaId"]),
        Index(value = ["categoriaId"])
    ]
)
data class MovimentacaoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val descricao: String,
    val valor: Double,
    val tipo: String,
    val contaId: Int,
    val categoriaId: Int,
    val data: String
)