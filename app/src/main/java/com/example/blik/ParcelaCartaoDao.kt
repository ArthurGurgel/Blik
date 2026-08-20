package com.example.blik

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelaCartaoDao {

    @Insert
    suspend fun inserirTodas(
        parcelas: List<ParcelaCartaoEntity>
    )

    @Query(
        """
        SELECT *
        FROM parcelas_cartao
        ORDER BY
            anoFatura ASC,
            mesFatura ASC,
            numeroParcela ASC
        """
    )
    fun listarTodas(): Flow<List<ParcelaCartaoEntity>>

    @Query(
        """
        SELECT *
        FROM parcelas_cartao
        WHERE cartaoId = :cartaoId
        AND mesFatura = :mes
        AND anoFatura = :ano
        ORDER BY numeroParcela ASC
        """
    )
    fun listarPorFatura(
        cartaoId: Int,
        mes: Int,
        ano: Int
    ): Flow<List<ParcelaCartaoEntity>>

    @Query(
        """
        DELETE FROM parcelas_cartao
        WHERE movimentacaoId = :movimentacaoId
        """
    )
    suspend fun excluirPorMovimentacao(
        movimentacaoId: Int
    )
}


