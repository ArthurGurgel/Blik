package com.example.minhasfinancas

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimentacoes")
data class MovimentacaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val descricao: String,
    val valor: Double,
    val tipo: String,
    val conta: String,
    val categoria: String,
    val data: String
)