package com.example.blik

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
    SELECT
        movimentacoes.id AS id,
        movimentacoes.descricao AS descricao,
        movimentacoes.valor AS valor,
        movimentacoes.tipo AS tipo,
        movimentacoes.contaId AS contaId,
        contas.nome AS contaNome,
        movimentacoes.categoria AS categoria,
        movimentacoes.data AS data

    FROM movimentacoes

    INNER JOIN contas
        ON movimentacoes.contaId = contas.id

    ORDER BY movimentacoes.id DESC
    """
    )
    fun listarTodas(): Flow<List<MovimentacaoComConta>>

    @Query(
        """
    SELECT COUNT(*)
    FROM movimentacoes
    WHERE contaId = :contaId
    """
    )
    suspend fun quantidadePorConta(
        contaId: Int
    ): Int
}