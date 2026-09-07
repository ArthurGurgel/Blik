package com.example.blik

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface OrcamentoDao {


    @Insert(
        onConflict =
            OnConflictStrategy.IGNORE
    )
    suspend fun inserir(
        orcamento: OrcamentoEntity
    ): Long


    @Query(
        """
        SELECT *
        FROM orcamentos
        ORDER BY ano DESC,
                 mes DESC,
                 tipo ASC
        """
    )
    fun listarTodos():
            Flow<List<OrcamentoEntity>>


    @Query(
        """
        SELECT *
        FROM orcamentos
        WHERE mes = :mes
          AND ano = :ano
          AND ativo = 1
        ORDER BY
            CASE
                WHEN tipo = 'TOTAL'
                THEN 0
                ELSE 1
            END,
            id ASC
        """
    )
    fun listarPorPeriodo(
        mes: Int,
        ano: Int
    ): Flow<List<OrcamentoEntity>>


    @Query(
        """
        SELECT *
        FROM orcamentos
        WHERE referencia = :referencia
        LIMIT 1
        """
    )
    suspend fun buscarPorReferencia(
        referencia: String
    ): OrcamentoEntity?


    @Query(
        """
        UPDATE orcamentos
        SET limiteMensal = :novoLimite,
            ativo = 1
        WHERE id = :id
        """
    )
    suspend fun editarLimite(
        id: Int,
        novoLimite: Double
    )


    @Query(
        """
        UPDATE orcamentos
        SET ativo = :ativo
        WHERE id = :id
        """
    )
    suspend fun definirAtivo(
        id: Int,
        ativo: Boolean
    )


    @Query(
        """
        DELETE FROM orcamentos
        WHERE id = :id
        """
    )
    suspend fun excluir(
        id: Int
    )


    // =================================================
    // SINCRONIZAÇÃO
    // =================================================

    @Query(
        """
        SELECT *
        FROM orcamentos
        WHERE syncId IS NULL
        """
    )
    suspend fun listarSemSyncId():
            List<OrcamentoEntity>


    @Query(
        """
        UPDATE orcamentos
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
        SELECT *
        FROM orcamentos
        """
    )
    suspend fun listarTodosUmaVez():
            List<OrcamentoEntity>


    @Query(
        """
        SELECT *
        FROM orcamentos
        WHERE syncId = :syncId
        LIMIT 1
        """
    )
    suspend fun buscarPorSyncId(
        syncId: String
    ): OrcamentoEntity?

    @Query(
        """
    UPDATE orcamentos

    SET referencia = :referencia,
        mes = :mes,
        ano = :ano,
        tipo = :tipo,
        categoriaId = :categoriaId,
        limiteMensal = :limiteMensal,
        ativo = :ativo

    WHERE syncId = :syncId
    """
    )
    suspend fun atualizarDaNuvem(

        syncId: String,

        referencia: String,

        mes: Int,

        ano: Int,

        tipo: String,

        categoriaId: Int?,

        limiteMensal: Double,

        ativo: Boolean

    ): Int


    @Query(
        """
        DELETE FROM orcamentos
        WHERE syncId = :syncId
        """
    )
    suspend fun excluirPorSyncId(
        syncId: String
    ): Int
}