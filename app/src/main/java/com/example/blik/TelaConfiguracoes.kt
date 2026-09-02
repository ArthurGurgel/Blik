package com.example.blik

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaConfiguracoes(
    modoTema: ModoTema,
    onModoTemaAlterado: (ModoTema) -> Unit,
    planoAtual: PlanoUsuario,
    onVoltar: () -> Unit
) {

    BackHandler {
        onVoltar()
    }


    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(

                navigationIcon = {

                    IconButton(
                        onClick =
                            onVoltar
                    ) {

                        Text(
                            text = "‹",
                            fontSize = 34.sp
                        )
                    }
                },

                title = {

                    Text(
                        text =
                            "Configurações",

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            )
        }

    ) { innerPadding ->

        LazyColumn(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .padding(
                        horizontal = 20.dp
                    )
                    .fillMaxSize()
        ) {

            item {

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )


                Text(
                    text = "Aparência",
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )


                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .surface
                        )
                ) {

                    Column {

                        OpcaoTema(
                            titulo =
                                "Seguir sistema",

                            descricao =
                                "Usa o tema definido no Android",

                            selecionado =
                                modoTema ==
                                        ModoTema.SISTEMA,

                            onClick = {

                                onModoTemaAlterado(
                                    ModoTema.SISTEMA
                                )
                            }
                        )


                        HorizontalDivider()


                        OpcaoTema(
                            titulo =
                                "Claro",

                            descricao =
                                "Mantém o Blik sempre claro",

                            selecionado =
                                modoTema ==
                                        ModoTema.CLARO,

                            onClick = {

                                onModoTemaAlterado(
                                    ModoTema.CLARO
                                )
                            }
                        )


                        HorizontalDivider()


                        OpcaoTema(
                            titulo =
                                "Escuro",

                            descricao =
                                "Mantém o Blik sempre escuro",

                            selecionado =
                                modoTema ==
                                        ModoTema.ESCURO,

                            onClick = {

                                onModoTemaAlterado(
                                    ModoTema.ESCURO
                                )
                            }
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(28.dp)
                )


                Text(
                    text = "Plano",
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )


                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .surface
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(18.dp)
                    ) {

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column {

                                Text(
                                    text =
                                        when (planoAtual) {

                                            PlanoUsuario.FREE ->
                                                "Blik Free"

                                            PlanoUsuario.PRO ->
                                                "Blik Pro"

                                            PlanoUsuario.PRO_CONECTADO ->
                                                "Blik Pro Conectado"
                                        },

                                    fontSize =
                                        16.sp,

                                    fontWeight =
                                        FontWeight.SemiBold
                                )


                                Text(
                                    text =
                                        "Plano atual",

                                    fontSize =
                                        12.sp,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                            }


                            Text(
                                text =
                                    when (planoAtual) {

                                        PlanoUsuario.FREE ->
                                            "FREE"

                                        PlanoUsuario.PRO ->
                                            "PRO"

                                        PlanoUsuario.PRO_CONECTADO ->
                                            "PRO+"
                                    },
                                fontSize = 12.sp,
                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )


                        HorizontalDivider()


                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )


                        Text(
                            text =
                                "Blik Pro",

                            fontSize =
                                16.sp,

                            fontWeight =
                                FontWeight.SemiBold
                        )


                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )


                        Text(
                            text =
                                "Orçamentos, relatórios, automações " +
                                        "e integrações avançadas serão " +
                                        "adicionados futuramente.",

                            fontSize =
                                13.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(28.dp)
                )


                Text(
                    text =
                        "Recursos futuros",

                    fontSize =
                        18.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )


                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .surface
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(18.dp)
                    ) {

                        RecursoFuturo(
                            nome =
                                "Orçamentos mensais",

                            tipo =
                                "PRO"
                        )


                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    vertical = 12.dp
                                )
                        )


                        RecursoFuturo(
                            nome =
                                "Notificações",

                            tipo =
                                "EM BREVE"
                        )


                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    vertical = 12.dp
                                )
                        )


                        RecursoFuturo(
                            nome =
                                "Relatórios",

                            tipo =
                                "PRO"
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(40.dp)
                )
            }
        }
    }
}


@Composable
private fun OpcaoTema(
    titulo: String,
    descricao: String,
    selecionado: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        RadioButton(
            selected =
                selecionado,

            onClick =
                onClick
        )


        Column(
            modifier =
                Modifier.padding(
                    start = 8.dp
                )
        ) {

            Text(
                text =
                    titulo,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Medium
            )


            Text(
                text =
                    descricao,

                fontSize =
                    12.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


@Composable
private fun RecursoFuturo(
    nome: String,
    tipo: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                nome,

            fontSize =
                14.sp
        )


        Text(
            text =
                tipo,

            fontSize =
                11.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                if (
                    tipo == "PRO"
                ) {
                    MaterialTheme
                        .colorScheme
                        .primary
                } else {
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                }
        )
    }
}