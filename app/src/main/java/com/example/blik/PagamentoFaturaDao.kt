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
}