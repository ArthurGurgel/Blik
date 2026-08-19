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
        movimentacoes.categoriaId AS categoriaId,
        categorias.nome AS categoriaNome,
        movimentacoes.data AS data

    FROM movimentacoes

    INNER JOIN contas
        ON movimentacoes.contaId = contas.id
        
    INNER JOIN categorias
        ON movimentacoes.categoriaId = categorias.id

    ORDER BY 
        substr(movimentacoes.data, 7, 4) DESC,
        substr(movimentacoes.data, 4, 2) DESC,
        substr(movimentacoes.data, 1, 2) DESC,
        movimentacoes.id DESC
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

    @Query(
        """
    SELECT COUNT(*)
    FROM movimentacoes
    WHERE categoriaId = :categoriaId
    """
    )
    suspend fun quantidadePorCategoria(
        categoriaId: Int
    ): Int

    @Query(
        """
    DELETE FROM movimentacoes
    WHERE id = :id
    """
    )
    suspend fun excluir(
        id: Int
    )

    @Query(
        """
    UPDATE movimentacoes
    SET
        descricao = :descricao,
        valor = :valor,
        tipo = :tipo,
        contaId = :contaId,
        categoriaId = :categoriaId,
        data = :data
    WHERE id = :id
    """
    )
    suspend fun editar(
        id: Int,
        descricao: String,
        valor: Double,
        tipo: String,
        contaId: Int,
        categoriaId: Int,
        data: String
    )
}