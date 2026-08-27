package com.example.blik

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartaoDao {
    @Insert(
        onConflict = OnConflictStrategy.IGNORE
    )
    suspend fun inserir(
        cartao: CartaoEntity
    ): Long
    @Query(
        """
            SELECT
                cartoes.id AS id,
                cartoes.nome AS nome,
                cartoes.limite AS limite,
                cartoes.diaFechamento AS diaFechamento,
                cartoes.diaVencimento AS diaVencimento,
                cartoes.contaId AS contaId,
                contas.nome AS contaNome
                
            FROM cartoes
            
            INNER JOIN contas
                ON cartoes.contaId = contas.id
                
            ORDER BY cartoes.nome ASC
        """
    )
    fun listarTodos(): Flow<List<CartaoComConta>>

    @Query(
        """
            SELECT COUNT(*)
            FROM cartoes
            WHERE LOWER(nome) = LOWER(:nome)
        """
    )
    suspend fun existeNome(
        nome: String
    ): Int

    @Query(
        """
            SELECT COUNT(*)
            FROM cartoes
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
            UPDATE cartoes
            SET
                nome = :nome,
                limite = :limite,
                diaFechamento = :diaFechamento,
                diaVencimento = :diaVencimento,
                contaId = :contaId
            WHERE id = :id
        """
    )
    suspend fun editar(
        id: Int,
        nome: String,
        limite: Double,
        diaFechamento: Int,
        diaVencimento: Int,
        contaId: Int
    )

    @Query(
        """
            DELETE FROM cartoes
            WHERE id = :id
        """
    )
    suspend fun excluir(
        id: Int
    )

    @Query(
        """
    SELECT * FROM cartoes
    WHERE syncId IS NULL
    """
    )
    suspend fun listarSemSyncId(): List<CartaoEntity>

    @Query(
        """
    UPDATE cartoes
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
    SELECT * FROM cartoes
    """
    )
    suspend fun listarTodosUmaVez(): List<CartaoEntity>
}