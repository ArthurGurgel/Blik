package com.example.minhasfinancas

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimentacaoDao {
    @Insert
    suspend fun inserir(
        movimentacao: MovimentacaoEntity
    )

    @Query(
        """
            SELECT *
            FROM movimentacoes
            ORDER BY id DESC
        """
    )
    fun listarTodas(): Flow<List<MovimentacaoEntity>>

    @Query(
        """
            SELECT COUNT(*)
            FROM movimentacoes
            WHERE LOWER(conta) = LOWER(:nomeConta)
        """
    )
    suspend fun quantidadePorConta(
        nomeConta: String
    ): Int
}