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
            entity = ContaEntity::class,
            parentColumns = ["id"],
            childColumns = ["contaDestinoId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = CartaoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cartaoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["contaId"]),
        Index(value = ["contaDestinoId"]),
        Index(value = ["categoriaId"]),
        Index(value = ["cartaoId"]),
        Index(
            value = ["syncId"],
            unique = true
        )
    ]
)
data class MovimentacaoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val descricao: String,

    val valor: Double,

    val tipo: String,

    val formaPagamento: String? = null,

    val contaId: Int? = null,

    val contaDestinoId: Int? = null,

    val categoriaId: Int? = null,

    val cartaoId: Int? = null,

    val quantidadeParcelas: Int = 1,

    val data: String,
    val syncId: String? =
        java.util.UUID.randomUUID().toString()
)