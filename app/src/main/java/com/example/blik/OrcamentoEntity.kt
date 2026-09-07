package com.example.blik

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "orcamentos",

    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(
            value = ["referencia"],
            unique = true
        ),

        Index(
            value = ["categoriaId"]
        ),

        Index(
            value = ["syncId"],
            unique = true
        )
    ]
)
data class OrcamentoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,


    /*
     * Chave estável que impede duplicidade.
     *
     * Exemplos:
     * 2026-09-TOTAL
     * 2026-09-CATEGORIA-UUID_DA_CATEGORIA
     */
    val referencia: String,


    val mes: Int,

    val ano: Int,


    /*
     * TOTAL
     * CATEGORIA
     */
    val tipo: String,


    /*
     * null quando tipo == TOTAL
     */
    val categoriaId: Int? = null,


    val limiteMensal: Double,


    val ativo: Boolean = true,


    val syncId: String? =
        java.util.UUID
            .randomUUID()
            .toString()
)