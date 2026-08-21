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

    @Query(
        """
        SELECT
            parcelas_cartao.id AS id,
            parcelas_cartao.movimentacaoId AS movimentacaoId,
            movimentacoes.descricao AS descricao,
            parcelas_cartao.cartaoId AS cartaoId,
            cartoes.nome AS cartaoNome,
            parcelas_cartao.numeroParcela AS numeroParcela,
            parcelas_cartao.totalParcelas AS totalParcelas,
            parcelas_cartao.valor AS valor,
            parcelas_cartao.mesFatura AS mesFatura,
            parcelas_cartao.anoFatura AS anoFatura
        FROM parcelas_cartao
        INNER JOIN movimentacoes
            ON parcelas_cartao.movimentacaoId = movimentacoes.id
        INNER JOIN cartoes
            ON parcelas_cartao.cartaoId = cartoes.id
        ORDER BY
            parcelas_cartao.anoFatura DESC,
            parcelas_cartao.mesFatura DESC,
            cartoes.nome ASC,
            parcelas_cartao.id ASC
        """
    )
    fun listarComDetalhes(): Flow<List<ParcelaCartaoComDetalhes>>
}


