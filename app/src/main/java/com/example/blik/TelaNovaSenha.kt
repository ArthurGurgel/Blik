package com.example.blik

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun TelaNovaSenha(
    carregando: Boolean = false,
    mensagemErro: String? = null,
    onSalvar: (
        novaSenha: String
    ) -> Unit
) {

    var novaSenha by remember {
        mutableStateOf("")
    }

    var confirmarSenha by remember {
        mutableStateOf("")
    }

    var erroLocal by remember {
        mutableStateOf<String?>(null)
    }


    Scaffold(
        containerColor =
            MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp)
                    .fillMaxSize(),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            MarcaBlik(
                modifier =
                    Modifier
                        .width(130.dp)
                        .height(52.dp)
            )


            Spacer(
                modifier =
                    Modifier.height(40.dp)
            )


            Text(
                text =
                    "Criar nova senha",

                fontSize =
                    26.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme
                        .onBackground
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(
                text =
                    "Digite uma nova senha para sua conta.",

                fontSize =
                    14.sp,

                textAlign =
                    TextAlign.Center,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )


            Spacer(
                modifier =
                    Modifier.height(32.dp)
            )


            OutlinedTextField(
                value =
                    novaSenha,

                onValueChange = {
                    novaSenha = it
                    erroLocal = null
                },

                label = {
                    Text(
                        "Nova senha"
                    )
                },

                singleLine =
                    true,

                visualTransformation =
                    PasswordVisualTransformation(),

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Password
                    ),

                modifier =
                    Modifier.fillMaxWidth()
            )


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            OutlinedTextField(
                value =
                    confirmarSenha,

                onValueChange = {
                    confirmarSenha = it
                    erroLocal = null
                },

                label = {
                    Text(
                        "Confirmar nova senha"
                    )
                },

                singleLine =
                    true,

                visualTransformation =
                    PasswordVisualTransformation(),

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Password
                    ),

                modifier =
                    Modifier.fillMaxWidth()
            )


            val mensagem =
                erroLocal
                    ?: mensagemErro


            if (
                !mensagem
                    .isNullOrBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )


                Text(
                    text =
                        mensagem,

                    color =
                        MaterialTheme.colorScheme.error,

                    fontSize =
                        13.sp,

                    textAlign =
                        TextAlign.Center
                )
            }


            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )


            Button(
                enabled =
                    !carregando,

                onClick = {

                    when {

                        novaSenha.isBlank() -> {

                            erroLocal =
                                "Digite a nova senha."
                        }


                        novaSenha.length < 6 -> {

                            erroLocal =
                                "A senha deve possuir pelo menos 6 caracteres."
                        }


                        novaSenha !=
                                confirmarSenha -> {

                            erroLocal =
                                "As senhas não coincidem."
                        }


                        else -> {

                            erroLocal = null

                            onSalvar(
                                novaSenha
                            )
                        }
                    }
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
            ) {

                if (carregando) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier
                                .width(22.dp)
                                .height(22.dp),

                        strokeWidth =
                            2.dp,

                        color =
                            MaterialTheme.colorScheme
                                .onPrimary
                    )

                } else {

                    Text(
                        text =
                            "Alterar senha",

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }
    }
}