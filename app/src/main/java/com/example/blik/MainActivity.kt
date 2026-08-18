package com.example.blik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import com.example.blik.ui.theme.BlikTheme
import kotlinx.coroutines.launch

data class Movimentacao(
    val descricao: String,
    val valor: Double,
    val tipo: String,
    val contaId: Int,
    val contaNome: String,
    val categoria: String,
    val data: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BlikTheme {
                AppFinanceiro()
            }
        }
    }
}

@Composable
fun AppFinanceiro() {

    val context = LocalContext.current

    val banco = remember {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "blik.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    val dao = banco.movimentacaoDao()
    val contaDao = banco.contaDao()

    val movimentacoesEntity by dao
        .listarTodas()
        .collectAsState(initial = emptyList())

    val contas by contaDao
        .listarAtivas()
        .collectAsState(initial = emptyList())

    val todasContas by contaDao
        .listarTodas()
        .collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        if (contaDao.quantidade() == 0) {
            contaDao.inserirTodas(
                listOf(
                    ContaEntity(nome = "Banco do Brasil")
                )
            )
        }
    }

    val scope = rememberCoroutineScope()

    var telaAtual by remember {
        mutableStateOf("inicio")
    }

    val movimentacoes = movimentacoesEntity.map { item ->

        Movimentacao(
            descricao = item.descricao,
            valor = item.valor,
            tipo = item.tipo,
            contaId = item.contaId,
            contaNome = item.contaNome,
            categoria = item.categoria,
            data = item.data
        )
    }

    when (telaAtual) {

        "inicio" -> {
            TelaInicial(
                movimentacoes = movimentacoes,
                onNovaMovimentacao = {
                    telaAtual = "nova_movimentacao"
                },
                onContas = {
                    telaAtual = "contas"
                }
            )
        }

        "nova_movimentacao" -> {
            TelaNovaMovimentacao(
                contas = contas,

                onSalvar = { novaMovimentacao ->

                    scope.launch {
                        dao.inserir(
                            MovimentacaoEntity(
                                descricao = novaMovimentacao.descricao,
                                valor = novaMovimentacao.valor,
                                tipo = novaMovimentacao.tipo,
                                contaId = novaMovimentacao.contaId,
                                categoria = novaMovimentacao.categoria,
                                data = novaMovimentacao.data
                            )
                        )
                        telaAtual = "inicio"
                    }
                },

                onVoltar = {
                    telaAtual = "inicio"
                }
            )
        }
        "contas" -> {
            TelaContas(
                contas = todasContas,

                onAdicionarConta = { nome, resultado ->
                    scope.launch {
                        val nomeLimpo =
                            nome.trim()

                        val existe =
                            contaDao.existeNome(nomeLimpo) > 0

                        if (existe) {
                            resultado(false)
                        } else {
                            contaDao.inserir(
                                ContaEntity(
                                    nome = nomeLimpo
                                )
                            )

                            resultado(true)
                        }
                    }
                },

                onDesativar = { conta->
                    scope.launch {
                        contaDao.desativar(conta.id)
                    }
                },

                onReativar = { conta ->
                    scope.launch {
                        contaDao.reativar(conta.id)
                    }
                },

                onExcluir = { conta, resultado ->
                    scope.launch {
                        val quantidadeMovimentacoes =
                            dao.quantidadePorConta(conta.id)

                        if (quantidadeMovimentacoes > 0){
                            resultado(
                                false,
                                quantidadeMovimentacoes
                            )
                        } else {
                            contaDao.excluir(conta.id)

                            resultado(
                                true,
                                0
                            )
                        }
                    }
                },

                onVoltar = {
                    telaAtual = "inicio"
                }
            )
        }
    }
}

@Composable
fun TelaInicial(
    movimentacoes: List<Movimentacao>,
    onNovaMovimentacao: () -> Unit,
    onContas: () -> Unit
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {

            Text(
                text = "Blik",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Saldo atual",
                        fontSize = 16.sp
                    )

                    Text(
                        text = "R$ 3.450,00",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text("Entradas")

                    Text(
                        text = "R$ 5.200,00",
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("Saídas")

                    Text(
                        text = "R$ 1.750,00",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Text(
                text = "Contas",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            ContaItem(
                nome = "BRB",
                saldo = "R$ 1.200,00"
            )

            ContaItem(
                nome = "NuBank",
                saldo = "R$ 850,00"
            )

            ContaItem(
                nome = "C6 Bank",
                saldo = "R$ 700,00"
            )

            ContaItem(
                nome = "XP",
                saldo = "R$ 700,00"
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Movimentacoes",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (movimentacoes.isEmpty()) {
                Text(
                    text = "Nenhuma movimentacao cadastrada."
                )
            } else {
                movimentacoes.forEach { movimentacao ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = movimentacao.descricao,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text =
                                        if (movimentacao.tipo == "Entrada") {
                                            "+ R$ %.2f".format(movimentacao.valor)
                                        } else {
                                            "- R$ %.2f".format(movimentacao.valor)
                                        },
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "${movimentacao.contaNome} • ${movimentacao.categoria}"
                            )

                            Text(
                                text = movimentacao.data,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onContas,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gerenciar contas")
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
            Button(
                onClick = onNovaMovimentacao,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Nova movimentação")
            }
        }
    }
}

@Composable
fun TelaNovaMovimentacao(
    contas: List<ContaEntity>,
    onSalvar: (Movimentacao) -> Unit,
    onVoltar: () -> Unit
) {

    var tipo by remember {
        mutableStateOf("Saída")
    }

    var descricao by remember {
        mutableStateOf("")
    }

    var valor by remember {
        mutableStateOf("")
    }

    var contaSelecionada by remember {
        mutableStateOf<ContaEntity?>(null)
    }

    LaunchedEffect(contas) {
        if (
            contaSelecionada == null &&
            contas.isNotEmpty()
        ) {
            contaSelecionada = contas.first()
        }
    }

    var categoria by remember {
        mutableStateOf("Alimentação")
    }

    var data by remember {
        mutableStateOf("17/08/2026")
    }

    var menuContaAberto by remember {
        mutableStateOf(false)
    }

    var menuCategoriaAberto by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {

            Text(
                text = "Nova Movimentação",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Tipo",
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = {
                        tipo = "Entrada"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Entrada")
                }

                Button(
                    onClick = {
                        tipo = "Saída"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Saída")
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = {
                    descricao = it
                },
                label = {
                    Text("Descrição")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = valor,
                onValueChange = {
                    valor = it
                },
                label = {
                    Text("Valor")
                },
                placeholder = {
                    Text("Ex.: 150,00")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = data,
                onValueChange = {
                    data = it
                },
                label = {
                    Text("Data")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = {
                        menuContaAberto = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text =
                            "Conta: ${contaSelecionada?.nome ?: "Selecione"}"
                    )
                }

                DropdownMenu(
                    expanded = menuContaAberto,
                    onDismissRequest = {
                        menuContaAberto = false
                    }
                ) {

                    contas.forEach { contaBanco ->

                        DropdownMenuItem(
                            text = {
                                Text(contaBanco.nome)
                            },
                            onClick = {
                                contaSelecionada = contaBanco
                                menuContaAberto = false
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = {
                        menuCategoriaAberto = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Categoria: $categoria")
                }

                DropdownMenu(
                    expanded = menuCategoriaAberto,
                    onDismissRequest = {
                        menuCategoriaAberto = false
                    }
                ) {

                    val categorias = listOf(
                        "Alimentação",
                        "Saúde",
                        "Assinaturas",
                        "Compras",
                        "Viagem",
                        "Combustível",
                        "Gasto veicular",
                        "Rolê",
                        "Outros"
                    )

                    categorias.forEach { nomeCategoria ->

                        DropdownMenuItem(
                            text = {
                                Text(nomeCategoria)
                            },
                            onClick = {
                                categoria = nomeCategoria
                                menuCategoriaAberto = false
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Tipo selecionado: $tipo"
            )

            Text(
                text = "Conta: ${contaSelecionada?.nome ?: "Nenhuma"}"
            )

            Text(
                text = "Categoria: $categoria"
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = {

                    val valorConvertido =
                        valor
                            .replace(",", ".")
                            .toDoubleOrNull()

                    val contaEscolhida = contaSelecionada

                    if (
                        descricao.isNotBlank() &&
                        valorConvertido != null &&
                        contaEscolhida != null
                    ) {

                        val movimentacao = Movimentacao(
                            descricao = descricao,
                            valor = valorConvertido,
                            tipo = tipo,
                            contaId = contaEscolhida.id,
                            contaNome = contaEscolhida.nome,
                            categoria = categoria,
                            data = data
                        )

                        onSalvar(movimentacao)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = onVoltar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar")
            }
        }
    }
}
@Composable
fun ContaItem(
    nome: String,
    saldo: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = nome,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = saldo,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TelaContas(
    contas: List<ContaEntity>,
    onAdicionarConta: (
        String, (Boolean) -> Unit
    ) -> Unit,
    onDesativar: (ContaEntity) -> Unit,
    onReativar: (ContaEntity) -> Unit,
    onExcluir: (
            ContaEntity,(Boolean, Int) -> Unit
            ) -> Unit,
    onVoltar: () -> Unit
) {

    var nomeNovaConta by remember {
        mutableStateOf("")
    }

    var mensagem by remember {
        mutableStateOf("")
    }

    var mensagemExclusao by remember {
        mutableStateOf("")
    }

    var contaParaExcluir by remember {
        mutableStateOf<ContaEntity?>(null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {

            Text(
                text = "Gerenciar contas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            OutlinedTextField(
                value = nomeNovaConta,

                onValueChange = {
                    nomeNovaConta = it
                    mensagem = ""
                },

                label = {
                    Text("Nome da nova conta")
                },

                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = {

                    if (nomeNovaConta.isNotBlank()) {

                        onAdicionarConta(
                            nomeNovaConta
                        ) { sucesso ->

                            if (sucesso) {

                                mensagem =
                                    "Conta adicionada."

                                nomeNovaConta = ""

                            } else {

                                mensagem =
                                    "Já existe uma conta com esse nome."
                            }
                        }
                    }
                },

                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar conta")
            }

            if (mensagem.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = mensagem
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Contas cadastradas",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            contas.forEach { conta ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = conta.nome,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text =
                                    if (conta.ativa) {
                                        "Ativa"
                                    } else {
                                        "Desativada"
                                    }
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        if (conta.ativa) {

                            Button(
                                onClick = {
                                    onDesativar(conta)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Desativar")
                            }

                        } else {

                            Button(
                                onClick = {
                                    onReativar(conta)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reativar")
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Button(
                            onClick = {
                                contaParaExcluir = conta
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Excluir definitivamente")
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = onVoltar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar")
            }
        }
    }

    contaParaExcluir?.let { conta ->
        AlertDialog(
            onDismissRequest = {
                contaParaExcluir = null
            },

            title = {
                Text(
                    text = "Excluir conta?"
                )
            },

            text = {
                Text(
                    text = "Deseja excluir a conta \"${conta.nome}\" definitivamente? Esta ação não pode ser desfeita."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        onExcluir(conta) { sucesso, quantidade ->
                            if (sucesso) {
                                mensagemExclusao =
                                    "Conta excluída com sucesso."
                            } else {
                                mensagemExclusao =
                                    "Não é possível excluir a conta \"${conta.nome}\". " +
                                            "Existem $quantidade movimentação(ões) vinculada(s) a ela." +
                                            "Desative a conta para impedir novos lançamentos sem perder o histórico."
                            }

                            contaParaExcluir = null
                        }
                    }
                ) {
                    Text("Excluir")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        contaParaExcluir = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )}
        if (mensagemExclusao.isNotBlank()) {
            AlertDialog(
                onDismissRequest = {
                    mensagemExclusao = ""
                },
                title = {
                    Text(
                        text =
                            if (
                                mensagemExclusao.startsWith("Conta excluída")
                            ) {
                                "Conta excluída"
                            } else {
                                "Não é possível excluir"
                            }
                    )
                },
                text = {
                    Text(
                        text = mensagemExclusao
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            mensagemExclusao = ""
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
