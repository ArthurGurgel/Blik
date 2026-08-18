package com.example.blik

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MovimentacaoEntity::class,
        ContaEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movimentacaoDao(): MovimentacaoDao
    abstract fun contaDao(): ContaDao
}