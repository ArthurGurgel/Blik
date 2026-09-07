package com.example.blik

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MovimentacaoEntity::class,
        ContaEntity::class,
        CategoriaEntity::class,
        CartaoEntity::class,
        ParcelaCartaoEntity::class,
        PagamentoFaturaEntity::class,
        OrcamentoEntity::class
    ],
    version = 15,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movimentacaoDao(): MovimentacaoDao
    abstract fun contaDao(): ContaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun cartaoDao(): CartaoDao
    abstract fun parcelaCartaoDao(): ParcelaCartaoDao
    abstract fun pagamentoFaturaDao(): PagamentoFaturaDao
    abstract fun orcamentoDao(): OrcamentoDao
}