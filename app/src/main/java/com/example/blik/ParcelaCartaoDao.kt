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
            parcelas_cartao.anoFatura AS anoFatura,
            parcelas_cartao.quitadaAnteriormente AS quitadaAnteriormente
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
    @Query(
        """
    SELECT * FROM parcelas_cartao
    WHERE syncId IS NULL
    """
    )
    suspend fun listarSemSyncId(): List<ParcelaCartaoEntity>

    @Query(
        """
    UPDATE parcelas_cartao
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
    SELECT * FROM parcelas_cartao
    """
    )
    suspend fun listarTodasUmaVez(): List<ParcelaCartaoEntity>

    @Insert
    suspend fun inserir(
        parcela: ParcelaCartaoEntity
    ): Long


    @Query(
        """
    SELECT *
    FROM parcelas_cartao
    WHERE syncId = :syncId
    LIMIT 1
    """
    )
    suspend fun buscarPorSyncId(
        syncId: String
    ): ParcelaCartaoEntity?


    @Query(
        """
    UPDATE parcelas_cartao
    SET
        movimentacaoId = :movimentacaoId,
        cartaoId = :cartaoId,
        numeroParcela = :numeroParcela,
        totalParcelas = :totalParcelas,
        valor = :valor,
        mesFatura = :mesFatura,
        anoFatura = :anoFatura,
        quitadaAnteriormente = :quitadaAnteriormente
    WHERE id = :id
    """
    )
    suspend fun atualizarDaNuvem(
        id: Int,
        movimentacaoId: Int,
        cartaoId: Int,
        numeroParcela: Int,
        totalParcelas: Int,
        valor: Double,
        mesFatura: Int,
        anoFatura: Int,
        quitadaAnteriormente: Boolean
    )

    @Query(
        """
    DELETE FROM parcelas_cartao
    WHERE syncId = :syncId
    """
    )
    suspend fun excluirPorSyncId(
        syncId: String
    ): Int
}


