package com.example.blik

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categorias",
    indices = [
        Index(
            value = ["nome"],
            unique = true
        ),
        Index(
            value = ["syncId"],
            unique = true
        )
    ]
)
data class CategoriaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String,
    val syncId: String? =
        java.util.UUID.randomUUID().toString()

)