package com.example.minhasfinancas

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contas",
    indices = [
        Index(
            value = ["nome"],
            unique = true
        )
    ]
)
data class ContaEntity(

    @PrimaryKey(autoGenerate = true)

    val id: Int = 0,
    val nome: String,
    val saldoInicial: Double = 0.0,
    val ativa: Boolean = true
)