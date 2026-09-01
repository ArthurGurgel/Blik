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
import androidx.compose.material3.TextButton
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
fun TelaAutenticacao(
    carregando: Boolean = false,
    mensagemErro: String? = null,

    onEntrar: (
        email: String,
        senha: String
    ) -> Unit,

    onCadastrar: (
        email: String,
        senha: String
    ) -> Unit,

    onRecuperarSenha: (
        email: String
    ) -> Unit = {},
) {

    var modoCadastro by remember {
        mutableStateOf(false)
    }

    var email by remember {
        mutableStateOf("")
    }

    var senha by remember {
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
                modifier = Modifier.height(40.dp)
            )

            Text(
                text =
                    if (modoCadastro) {
                        "Crie sua conta"
                    } else {
                        "Bem-vindo ao Blik"
                    },

                fontSize = 26.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    if (modoCadastro) {
                        "Comece a organizar sua vida financeira."
                    } else {
                        "Entre para acessar suas finanças."
                    },

                fontSize = 14.sp,

                textAlign = TextAlign.Center,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            OutlinedTextField(
                value = email,

                onValueChange = {
                    email = it
                    erroLocal = null
                },

                label = {
                    Text("E-mail")
                },

                singleLine = true,

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Email
                    ),

                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            OutlinedTextField(
                value = senha,

                onValueChange = {
                    senha = it
                    erroLocal = null
                },

                label = {
                    Text("Senha")
                },

                singleLine = true,

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
            if (!modoCadastro) {

                TextButton(
                    enabled = !carregando,

                    onClick = {

                        val emailLimpo =
                            email.trim()

                        if (emailLimpo.isBlank()) {

                            erroLocal =
                                "Informe seu e-mail para recuperar a senha."

                        } else {

                            erroLocal = null

                            onRecuperarSenha(
                                emailLimpo
                            )
                        }
                    },

                    modifier =
                        Modifier.align(
                            Alignment.End
                        )
                ) {

                    Text(
                        "Esqueci minha senha"
                    )
                }
            }

            if (modoCadastro) {

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = confirmarSenha,

                    onValueChange = {
                        confirmarSenha = it
                        erroLocal = null
                    },

                    label = {
                        Text("Confirmar senha")
                    },

                    singleLine = true,

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
            }

            val mensagem =
                erroLocal ?: mensagemErro

            if (!mensagem.isNullOrBlank()) {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = mensagem,

                    color =
                        MaterialTheme.colorScheme.error,

                    fontSize = 13.sp,

                    textAlign = TextAlign.Center
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {

                    val emailLimpo =
                        email.trim()

                    when {

                        emailLimpo.isBlank() -> {
                            erroLocal =
                                "Informe seu e-mail."
                        }

                        senha.isBlank() -> {
                            erroLocal =
                                "Informe sua senha."
                        }

                        senha.length < 6 -> {
                            erroLocal =
                                "A senha deve possuir pelo menos 6 caracteres."
                        }

                        modoCadastro &&
                                senha != confirmarSenha -> {

                            erroLocal =
                                "As senhas não coincidem."
                        }

                        modoCadastro -> {

                            erroLocal = null

                            onCadastrar(
                                emailLimpo,
                                senha
                            )
                        }

                        else -> {

                            erroLocal = null

                            onEntrar(
                                emailLimpo,
                                senha
                            )
                        }
                    }
                },

                enabled = !carregando,

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

                        strokeWidth = 2.dp,

                        color =
                            MaterialTheme.colorScheme.onPrimary
                    )

                } else {

                    Text(
                        text =
                            if (modoCadastro) {
                                "Criar conta"
                            } else {
                                "Entrar"
                            },

                        fontSize = 16.sp,

                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            TextButton(
                enabled = !carregando,

                onClick = {

                    modoCadastro =
                        !modoCadastro

                    erroLocal = null
                    confirmarSenha = ""
                }
            ) {

                Text(
                    text =
                        if (modoCadastro) {
                            "Já possui uma conta? Entrar"
                        } else {
                            "Ainda não possui conta? Criar conta"
                        }
                )
            }
        }
    }
}