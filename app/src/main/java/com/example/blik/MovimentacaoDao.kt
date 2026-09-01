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
    ): Long

    @Query(
        """
    SELECT
        movimentacoes.id AS id,
        movimentacoes.descricao AS descricao,
        movimentacoes.valor AS valor,
        movimentacoes.tipo AS tipo,
        movimentacoes.formaPagamento AS formaPagamento,

        movimentacoes.contaId AS contaId,
        contaOrigem.nome AS contaNome,

        movimentacoes.contaDestinoId AS contaDestinoId,
        contaDestino.nome AS contaDestinoNome,

        movimentacoes.categoriaId AS categoriaId,
        categorias.nome AS categoriaNome,

        movimentacoes.cartaoId AS cartaoId,
        cartoes.nome AS cartaoNome,

        movimentacoes.quantidadeParcelas AS quantidadeParcelas,

        movimentacoes.data AS data

    FROM movimentacoes

    LEFT JOIN contas AS contaOrigem
        ON movimentacoes.contaId = contaOrigem.id

    LEFT JOIN contas AS contaDestino
        ON movimentacoes.contaDestinoId = contaDestino.id

    LEFT JOIN categorias
        ON movimentacoes.categoriaId = categorias.id

    LEFT JOIN cartoes
        ON movimentacoes.cartaoId = cartoes.id

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
        formaPagamento = :formaPagamento,
        contaId = :contaId,
        contaDestinoId = :contaDestinoId,
        categoriaId = :categoriaId,
        cartaoId = :cartaoId,
        quantidadeParcelas = :quantidadeParcelas,
        data = :data
    WHERE id = :id
    """
    )
    suspend fun editar(
        id: Int,
        descricao: String,
        valor: Double,
        tipo: String,
        formaPagamento: String?,
        contaId: Int?,
        contaDestinoId: Int?,
        categoriaId: Int?,
        cartaoId: Int?,
        quantidadeParcelas: Int,
        data: String
    )

    @Query(
        """
    SELECT * FROM movimentacoes
    WHERE syncId IS NULL
    """
    )
    suspend fun listarSemSyncId(): List<MovimentacaoEntity>

    @Query(
        """
    UPDATE movimentacoes
    SET syncId = :syncId
    WHERE id = :id
      AND syncId IS NULL
    """
    )
    suspend fun definirSyncId(
        id: Int,
        syncId: String
    ): Int

    @Query(
        """
    SELECT * FROM movimentacoes
    """
    )
    suspend fun listarTodasUmaVez(): List<MovimentacaoEntity>

    @Query(
        """
    SELECT *
    FROM movimentacoes
    WHERE syncId = :syncId
    LIMIT 1
    """
    )
    suspend fun buscarPorSyncId(
        syncId: String
    ): MovimentacaoEntity?

    @Query(
        """
    DELETE FROM movimentacoes
    WHERE syncId = :syncId
    """
    )
    suspend fun excluirPorSyncId(
        syncId: String
    ): Int
}