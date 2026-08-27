package com.example.blik

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    suspend fun inserir(
        categoria: CategoriaEntity
    ): Long

    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    suspend fun inserirTodas(
        categorias: List<CategoriaEntity>
    )

    @Query(
        """
        SELECT *
        FROM categorias
        ORDER BY nome ASC
        """
    )
    fun listarTodas(): Flow<List<CategoriaEntity>>

    @Query(
        """
        SELECT COUNT(*)
        FROM categorias
        """
    )
    suspend fun quantidade(): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM categorias
        WHERE LOWER(nome) = LOWER(:nome)
        """
    )
    suspend fun existeNome(
        nome: String
    ): Int

    @Query(
        """
        DELETE FROM categorias
        WHERE id = :id
        """
    )
    suspend fun excluir(id: Int)

    @Query(
        """
        UPDATE categorias
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
        FROM categorias
        WHERE LOWER(nome) = LOWER(:nome)
        AND id != :idAtual
        """
    )
    suspend fun existeOutroNome(
        nome: String,
        idAtual: Int
    ): Int

    @Query(
        """
    SELECT * FROM categorias
    WHERE syncId IS NULL
    """
    )
    suspend fun listarSemSyncId(): List<CategoriaEntity>

    @Query(
        """
    UPDATE categorias
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
    SELECT * FROM categorias
    """
    )
    suspend fun listarTodasUmaVez(): List<CategoriaEntity>
}