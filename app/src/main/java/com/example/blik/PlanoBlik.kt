package com.example.blik


enum class PlanoUsuario {

    FREE,

    PRO,

    PRO_CONECTADO
}


enum class RecursoBlik {


    CONTAS,

    CATEGORIAS,

    MOVIMENTACOES,

    TRANSFERENCIAS,

    CARTOES,

    FATURAS,

    HISTORICO,

    TEMA,

    BACKUP,

    SINCRONIZACAO_BASICA,

    TUTORIAL,



    ORCAMENTO_TOTAL,

    ORCAMENTO_CATEGORIA,

    ALERTAS_ORCAMENTO,

    NOTIFICACOES_AVANCADAS,

    RELATORIOS_MENSAIS,

    RELATORIOS_ANUAIS,

    RELATORIOS_AVANCADOS,

    LANCAMENTOS_RECORRENTES,

    AUTOMACOES,

    PERSONALIZACAO_HOME,

    WHATSAPP_BOT,

    INTEGRACOES_TERCEIROS,

    BLIK_WEB,


    OPEN_FINANCE,

    IMPORTACAO_BANCARIA,

    DDA_BOLETOS
}


object PermissoesPlanoBlik {


    fun planoMinimo(
        recurso: RecursoBlik
    ): PlanoUsuario {

        return when (recurso) {



            RecursoBlik.CONTAS,
            RecursoBlik.CATEGORIAS,
            RecursoBlik.MOVIMENTACOES,
            RecursoBlik.TRANSFERENCIAS,
            RecursoBlik.CARTOES,
            RecursoBlik.FATURAS,
            RecursoBlik.HISTORICO,
            RecursoBlik.TEMA,
            RecursoBlik.BACKUP,
            RecursoBlik.SINCRONIZACAO_BASICA,
            RecursoBlik.TUTORIAL ->

                PlanoUsuario.FREE


            RecursoBlik.ORCAMENTO_TOTAL,
            RecursoBlik.ORCAMENTO_CATEGORIA,
            RecursoBlik.ALERTAS_ORCAMENTO,
            RecursoBlik.NOTIFICACOES_AVANCADAS,
            RecursoBlik.RELATORIOS_MENSAIS,
            RecursoBlik.RELATORIOS_ANUAIS,
            RecursoBlik.RELATORIOS_AVANCADOS,
            RecursoBlik.LANCAMENTOS_RECORRENTES,
            RecursoBlik.AUTOMACOES,
            RecursoBlik.PERSONALIZACAO_HOME,
            RecursoBlik.WHATSAPP_BOT,
            RecursoBlik.INTEGRACOES_TERCEIROS,
            RecursoBlik.BLIK_WEB ->

                PlanoUsuario.PRO



            RecursoBlik.OPEN_FINANCE,
            RecursoBlik.IMPORTACAO_BANCARIA,
            RecursoBlik.DDA_BOLETOS ->

                PlanoUsuario.PRO_CONECTADO
        }
    }


    fun podeUsar(
        plano: PlanoUsuario,
        recurso: RecursoBlik
    ): Boolean {

        val planoNecessario =
            planoMinimo(
                recurso
            )


        return nivelPlano(plano) >=
                nivelPlano(planoNecessario)
    }


    private fun nivelPlano(
        plano: PlanoUsuario
    ): Int {

        return when (plano) {

            PlanoUsuario.FREE ->
                0

            PlanoUsuario.PRO ->
                1

            PlanoUsuario.PRO_CONECTADO ->
                2
        }
    }
}