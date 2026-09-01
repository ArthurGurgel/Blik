package com.example.blik

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PagamentoFaturaDao {

    @Insert
    suspend fun inserir(
        pagamento: PagamentoFaturaEntity
    ): Long

    @Query(
        """
        SELECT *
        FROM pagamentos_fatura
        ORDER BY
            anoFatura DESC,
            mesFatura DESC,
            id DESC
        """
    )
    fun listarTodos():
            Flow<List<PagamentoFaturaEntity>>

    @Query(
        """
        DELETE FROM pagamentos_fatura
        WHERE id = :id
        """
    )
    suspend fun excluir(
        id: Int
    )

    @Query(
        """
            SELECT
                pagamentos_fatura.id AS id,
                pagamentos_fatura.cartaoId AS cartaoId,
                pagamentos_fatura.contaId AS contaId,
                contas.nome AS contaNome,
                pagamentos_fatura.mesFatura AS mesFatura,
                pagamentos_fatura.anoFatura AS anoFatura,
                pagamentos_fatura.valorPago AS valorPago,
                pagamentos_fatura.dataPagamento AS dataPagamento
            FROM pagamentos_fatura
            INNER JOIN contas
                ON pagamentos_fatura.contaId = contas.id
            ORDER BY
                pagamentos_fatura.anoFatura DESC,
                pagamentos_fatura.mesFatura DESC,
                pagamentos_fatura.id Desc
        """
    )


    fun listarComConta():
            Flow<List<PagamentoFaturaComConta>>

    @Query(
        """
    SELECT * FROM pagamentos_fatura
    WHERE syncId IS NULL
    """
    )
    suspend fun listarSemSyncId(): List<PagamentoFaturaEntity>

    @Query(
        """
    UPDATE pagamentos_fatura
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
    SELECT * FROM pagamentos_fatura
    """
    )
    suspend fun listarTodosUmaVez(): List<PagamentoFaturaEntity>

    @Query(
        """
    SELECT *
    FROM pagamentos_fatura
    WHERE syncId = :syncId
    LIMIT 1
    """
    )
    suspend fun buscarPorSyncId(
        syncId: String
    ): PagamentoFaturaEntity?


    @Query(
        """
    UPDATE pagamentos_fatura
    SET
        cartaoId = :cartaoId,
        contaId = :contaId,
        mesFatura = :mesFatura,
        anoFatura = :anoFatura,
        valorPago = :valorPago,
        dataPagamento = :dataPagamento
    WHERE id = :id
    """
    )
    suspend fun atualizarDaNuvem(
        id: Int,
        cartaoId: Int,
        contaId: Int,
        mesFatura: Int,
        anoFatura: Int,
        valorPago: Double,
        dataPagamento: String
    )

    @Query(
        """
    DELETE FROM pagamentos_fatura
    WHERE syncId = :syncId
    """
    )
    suspend fun excluirPorSyncId(
        syncId: String
    ): Int
}