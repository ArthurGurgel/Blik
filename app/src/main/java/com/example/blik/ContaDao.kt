package com.example.blik

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContaDao {
    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    suspend fun inserir(
        conta: ContaEntity
    ): Long

    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    suspend fun inserirTodas(
        contas: List<ContaEntity>
    )

    @Query(
        """
        SELECT *
        FROM contas
        WHERE ativa = 1
        ORDER BY nome ASC
        """
    )
    fun listarAtivas(): Flow<List<ContaEntity>>

    @Query(
        """
        SELECT *
        FROM contas
        ORDER BY ativa DESC, nome ASC
        """
    )
    fun listarTodas(): Flow<List<ContaEntity>>

    @Query(
        """
        SELECT COUNT(*)
        FROM contas
        """
    )
    suspend fun quantidade(): Int

    @Query(
        """
            UPDATE  contas
            SET ativa = 0
            WHERE id = :id
        """
    )
    suspend fun desativar(id: Int)

    @Query(
        """ UPDATE contas
            SET ativa = 1
            WHERE id = :id
        """
    )
    suspend fun reativar(id: Int)

    @Query(
        """
            DELETE FROM contas
            WHERE id = :id
        """
    )
    suspend fun excluir(id: Int)

    @Query(
        """
            SELECT COUNT(*)
            FROM contas
            WHERE LOWER(nome) = LOWER(:nome)
        """
    )
    suspend fun existeNome(nome: String): Int

    @Query(
        """
        SELECT *
        FROM contas
        WHERE id = :id
        LIMIT 1
        """

    )
    suspend fun buscarPorId(
        id: Int
    ): ContaEntity?

    @Query(
        """
        UPDATE contas
        SET nome = :novoNome
        WHERE id = :id
        """
    )
    suspend fun editarNome(
        id: Int,
        novoNome: String
    )

    @Query(
        """
        SELECT COUNT(*)
        FROM contas
        WHERE LOWER(nome) = LOWER(:nome)
        AND id != :idAtual
        """
    )
    suspend fun existeOutroNome(
        nome: String,
        idAtual: Int
    ): Int


}

